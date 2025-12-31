package me.dvyy.particles.compute.simulation

import de.fabmax.kool.math.PI_F
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.ParticleBuffers
import me.dvyy.particles.compute.forces.ForceBindings
import me.dvyy.particles.compute.forces.PairwiseForceFunction
import me.dvyy.particles.compute.helpers.KslFloat
import me.dvyy.particles.compute.helpers.cellId
import me.dvyy.particles.compute.helpers.forNearbyGridCells
import me.dvyy.particles.compute.helpers.types.particle
import me.dvyy.particles.compute.partitioning.WORK_GROUP_SIZE
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.dsl.ExportDataType

class FieldsShader(
    val configRepo: ConfigRepository,
    val buffers: ParticleBuffers,
    val forcesDef: ForceBindings,
    val pass: Int,
) {
    val shader = KslComputeShader("Fields $pass") {
        computeStage(WORK_GROUP_SIZE) {
            FieldsShaderProgram(this, forcesDef, configRepo).apply {
                main {
                    // Get the particle id from the global invocation (using only x as in GLSL)
                    val id = int1Var(inGlobalInvocationId.x.toInt1(), "id")
                    val params = structVar(params, "params")
                    val particle = particle(id, "particle") // Load current particle properties
                    // p(t + dt); since half step runs before this
                    // v(t + dt/2)
                    // a(t + dT)
                    val nextForce by Vec3f.ZERO.const

                    // === HELPER FUNCTIONS === TODO move out
                    fun cutoff(
                        distance: KslScalarExpression<KslFloat1>,
                        cutoffD: KslScalarExpression<KslFloat1>,
                        cutoffR: KslScalarExpression<KslFloat1>,
                    ): KslScalarExpression<KslFloat1> {
                        // Clamp the distance to the transition range [lowerBound, upperBound].
                        val clampedDistance = clamp(distance, cutoffR - cutoffD, cutoffR + cutoffD)
                        return 0.5f.const - (0.5f.const * sin((PI_F / 2f).const * (clampedDistance - cutoffR) / cutoffD))
                    }

                    fun KslFloat.clampMaxForce() = min(this, params[SimulationParametersStruct.maxForce])

                    // === Pre-pass to calculate data that requires neighbours ===
                    // TODO move into preprocess step for pairwise forces
                    // Calculate local neighbours (as in tersoff)
                    // Given distance between two particles, return a smoothed cutoff from 1 to 0
                    val localCount by 0f.const
                    forNearbyGridCells(particle.cell) { neighbourCell ->
                        neighbourCell.forEachParticle { other ->
                            `if`(all(other.position eq particle.position)) { `continue`() }
                            val direction by particle.position - other.position
                            val dist by length(direction)
                            localCount += cutoff(dist, 0.2f.const, 3f.const) //TODO cutoff function
                        }
                    }

                    // === Individual forces ===
                    // TODO apply maxForce to individual forces
                    individualForces.forEach { force ->
                        val interaction = structVar(force.interactionFor(particle.type))
                        // TODO avoid conditional branch
                        with(force) {
                            `if`(interaction[force.binding.interactionsStruct.enabled] eq 1f.const) {
                                nextForce += force.function.invoke(
                                    position = particle.position,
                                    parameters = interaction.parametersAsArray()
                                )
                            }
                        }
                    }

                    // === Pairwise forces ===
                    forNearbyGridCells(particle.cell) { neighbourCell ->
                        neighbourCell.forEachParticle { other ->

                            // TODO this branch slows down perf measurably.
                            //  On AMD it's not necessary as multiplication by zero wins, but on NVIDIA/Intel it causes an infinity.
                            //  It may be possible to optimize a good bit by only placing a conditional into the current cell and interating over all other cells
                            //  with a for loop, however this doubles generated code size.
                            `if`(all(other.position eq particle.position)) { `continue`() }

                            val direction by particle.position - other.position
                            val dist by length(direction)
                            val forceBetweenParticles by 0f.const

                            // Compute a hash based on the particle types
                            val pairHash = PairwiseForceFunction.pairHash(
                                particle.type,
                                other.type,
                                forceBindings.particleTypeCount
                            )

                            // Call invoke each pairwise force function with extracted parameters
                            pairwiseForces.forEach { force ->
                                //NOTE necessary for OPENGL to compile
                                //TODO this buffer access adds a good amount of overhead
                                val interaction = structVar(force.interactionFor(pairHash))
                                // For pairs without an interaction paramsMat[0][0] is 0
                                with(force) {
                                    // TODO avoid conditional branch, again on some vendors multiplication by zero may be nonzero
                                    `if`(interaction[force.binding.interactionsStruct.enabled] eq 1f.const) {
                                        forceBetweenParticles += force.function
                                            .invoke(
                                                distance = dist,
                                                localCount = localCount,
                                                parameters = interaction.parametersAsArray()
                                            )
                                            .clampMaxForce()
                                    }
                                }
                            }

                            nextForce += normalize(direction) * forceBetweenParticles
                        }
                    }


                    // --- Begin wall repulsion snippet ---
                    // Define simulation box boundaries
                    // Wall repulsion

                    //TODO make configurable, since lennardJones might not be provided
                    val lJ = wallForce
                    val extraDist = 0.1f.const // Add a small amount of distance so the force is always nonzero
                    nextForce.x += lJ(particle.position.x + extraDist)
                    nextForce.x -= lJ(boxMax.x - particle.position.x + extraDist)
                    nextForce.y += lJ(particle.position.y + extraDist)
                    nextForce.y -= lJ(boxMax.y - particle.position.y + extraDist)
                    `if`(boxMax.z ne 0f.const) {
                        nextForce.z += lJ(particle.position.z + extraDist)
                        nextForce.z -= lJ(boxMax.z - particle.position.z + extraDist)
                    }
                    // Cap force
                    `if`(length(nextForce) gt params[SimulationParametersStruct.maxForce]) {
                        nextForce set normalize(nextForce) * params[SimulationParametersStruct.maxForce]
                    }

                    // Compute next velocity with Verlet integration
                    val nextVelocity by particle.velocity + ((particle.currForce + nextForce) * dT / 2f.const)
                    // Cap velocity and net force to their maximum values
                    `if`(length(nextVelocity) gt params[SimulationParametersStruct.maxVelocity]) {
                        nextVelocity set normalize(nextVelocity) * params[SimulationParametersStruct.maxVelocity]
                    }

                    // === Nudge particles towards target velocity ===
                    //TODO using delegate for these generates `params.targetVelocity` as the field name, causing a compilation error on WGPU
                    val targetVelocity = float1Var(params[SimulationParametersStruct.targetVelocity])
                    val totalSqrtVelocities by velocityData[0.const]
                    val average by totalSqrtVelocities / count.toFloat1()
                    val strength = float1Var(params[SimulationParametersStruct.targetVelocityFixStrength])
                    nextVelocity set nextVelocity * sqrt(
                        1f.const + (dT * strength) * ((targetVelocity) / max(
                            average,
                            0.1f.const
                        ) - 1f.const)
                    )

                    forces[id] = float4Value(nextForce, 0f)
                    velocities[id] = float4Value(nextVelocity, 0f)

                    // === Export data buffer based on defined type ===
                    val exportDataType = int1Var(params[SimulationParametersStruct.exportDataType])
                    val rescaleBy = float1Var(params[SimulationParametersStruct.exportDataRescale])
                    `if`(exportDataType eq ExportDataType.LOCAL_NEIGHBOURS.ordinal.const) {
                        exportedData[id] = localCount * rescaleBy
                    }.elseIf(exportDataType eq ExportDataType.CELL_PARTICLE_COUNT.ordinal.const) {
                        exportedData[id] = (cellOffsetsEnd[cellId(particle.cell, gridCells)] - cellOffsets[cellId(particle.cell, gridCells)]).toFloat1() * rescaleBy
                    }
                }
            }
        }

    }

    // Uniforms
    var gridSize by shader.uniform1f("gridSize")
    var gridCells by shader.uniform3i("gridCells")
    var dT by shader.uniform1f("dT")
    var count by shader.uniform1i("count")
    var boxMax by shader.uniform3f("boxMax")
    var params = shader.uniformStruct("params", SimulationParametersStruct)

    // Storage buffers
    var particle2CellKey by shader.storage("particle2CellKey")
    var cellOffsets by shader.storage("cellOffsets")
    var cellOffsetsEnd by shader.storage("cellOffsetsEnd")
    var positions by shader.storage("positions")
    var velocities by shader.storage("velocities")
    var exportedData by shader.storage("exportedData")
    var forces by shader.storage("forces")
    var particleTypes by shader.storage("particleTypes")
    var velocityData by shader.storage("velocityData")
}
//
//context(scope: KslScopeBuilder)
//fun clampVector(vector: KslExprFloat3, maxLength: KslExprInt1): KslExprFloat3 = with(scope) {
//    val vectorLength = length(vector)
//    val scale = float1Var(maxLength.toFloat1() / max(vectorLength, maxLength.toFloat1()))
//    vector * scale
//}

package me.dvyy.particles.compute.simulation

import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.forces.ForceBindings
import me.dvyy.particles.compute.forces.KslForce
import me.dvyy.particles.config.ConfigRepository

class FieldsShaderProgram(
    val stage: KslComputeStage,
    val forceBindings: ForceBindings,
    private val configRepo: ConfigRepository,
) {
    val program: KslProgram = stage.program
    val is3d = configRepo.config.value.simulation.threeDimensions

    // Uniforms
    val gridSize = with(program) { uniformFloat1("gridSize") }
    val gridCells = with(program) { uniformInt3("gridCells") }
    val dT = with(program) { uniformFloat1("dT") }
    val count = with(program) { uniformInt1("count") }
    val params = with(program) { uniformStruct("params", SimulationParametersStruct) }
    val boxMax = with(program) { uniformFloat3("boxMax") }

    // Storage buffers
    val particle2CellKey = with(program) { storage<KslInt1>("particle2CellKey") }
    val cellOffsets = with(program) { storage<KslInt1>("cellOffsets") }
    val cellOffsetsEnd = with(program) { storage<KslInt1>("cellOffsetsEnd") }
    val positions = with(program) { storage<KslFloat4>("positions") }
    val velocities = with(program) { storage<KslFloat4>("velocities") }
    val forces = with(program) { storage<KslFloat4>("forces") }
    val exportedData = with(program) { storage<KslFloat1>("exportedData") }
    val velocityData = with(program) { storage<KslFloat1>("velocityData") }

    val particleTypes = with(program) { storage<KslInt1>("particleTypes") }

    // Define all force functions, create uniforms for their parameters
//    val forcesManager = ForcesManager(stage, forceBindings)
    val individualForces = forceBindings.individualForces.map { KslForce(it, stage) }
    val pairwiseForces = forceBindings.pairwiseForces.map { KslForce(it, stage) }

    val wallForce = with(stage) {
        functionFloat1("wall_force") {
            val distance = paramFloat1("distance")
            body {
                val sigma = 5f.const
                val epsilon = 0.0001f.const
                val invR = float1Var(sigma / distance, "invR")
                val invR6 = float1Var(invR * invR * invR * invR * invR * invR, "invR6")
                val invR12 = float1Var(invR6 * invR6, "invR12")
                24f.const * epsilon * (2f.const * invR12 - invR6) / distance
            }
        }
    }
}
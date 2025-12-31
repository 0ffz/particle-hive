package me.dvyy.particles.compute.simulation

import de.fabmax.kool.pipeline.ComputePass
import de.fabmax.kool.util.set
import me.dvyy.particles.compute.ParticleBuffers
import me.dvyy.particles.compute.data.MeanSquareVelocities
import me.dvyy.particles.compute.forces.ForceBindings
import me.dvyy.particles.config.ConfigRepository

class FieldsMultiPasses(
    val buffers: ParticleBuffers,
    val configRepo: ConfigRepository,
    val forcesDefinition: ForceBindings,
) {
    private fun initBuffers(fields: FieldsShader, halfStep: VerletHalfStepShader) {
        fields.gridSize = configRepo.gridSize
        fields.gridCells = configRepo.gridCells
        fields.count = configRepo.count
        fields.particleTypes = buffers.particleTypesBuffer
        fields.cellOffsets = buffers.offsetsBuffer
        fields.cellOffsetsEnd = buffers.offsetsEndBuffer
        fields.particle2CellKey = buffers.particleGridCellKeys
        fields.positions = buffers.positionBuffer
        fields.velocities = buffers.velocitiesBuffer
        fields.exportedData = buffers.exportedDataBuffer
        fields.forces = buffers.forcesBuffer
        fields.boxMax = configRepo.boxSize

        halfStep.positions = buffers.positionBuffer
        halfStep.velocities = buffers.velocitiesBuffer
        halfStep.forces = buffers.forcesBuffer
        halfStep.boxMax = configRepo.boxSize
    }

    fun addTo(
        computePass: ComputePass,
        velocitiesShader: MeanSquareVelocities,
    ): List<Pair<ComputePass.Task, ComputePass.Task>> {
        val config = configRepo.config.value

        val passes = buildList<Pair<ComputePass.Task, ComputePass.Task>> {
            repeat(config.simulation.passesPerFrame) { passIndex ->
                val halfStep = VerletHalfStepShader(passIndex)
                val halfStepTask = computePass.addTask(halfStep.shader, numGroups = configRepo.numGroups).apply {
                    onBeforeDispatch {
                        configRepo.whenDirty {
                            halfStep.dT = simulation.dT.toFloat()
                            numGroups.set(configRepo.numGroups)
                        }
                    }
                }
                val fields = FieldsShader(configRepo, buffers, forcesDefinition, passIndex)
                val fullStepTask = computePass.addTask(fields.shader, numGroups = configRepo.numGroups).apply {
                    onBeforeDispatch {
                        configRepo.whenDirty {
                            fields.dT = simulation.dT.toFloat()
                            fields.params.set {
                                it.maxVelocity.set(simulation.maxVelocity.toFloat())
                                it.maxForce.set(simulation.maxForce.toFloat())
                                it.targetVelocity.set(simulation.targetVelocity.toFloat())
                                it.targetVelocityFixStrength.set(simulation.targetVelocityStrength.toFloat())
                                it.exportDataType.set(simulation.exportData.type.ordinal)
                                it.exportDataRescale.set(simulation.exportData.rescale.toFloat())
                            }
                            val count = configRepo.count
                            fields.count = count
                            numGroups.set(configRepo.numGroups)
                        }
                        //TODO whenDirty
                        with(shader) {
                            forcesDefinition.forces.forEach {
                                it.uploadParameters()
                            }
                        }
                    }
                }
                initBuffers(fields, halfStep)
                fields.velocityData = velocitiesShader.output
                add(halfStepTask to fullStepTask)
            }
        }
        return passes
    }
}

package me.dvyy.particles.compute.data

import de.fabmax.kool.math.Vec3i
import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.*
import de.fabmax.kool.pipeline.ComputePass
import me.dvyy.particles.compute.ParticleBuffers
import me.dvyy.particles.compute.partitioning.WORK_GROUP_SIZE
import me.dvyy.particles.helpers.Buffers

class MeanSquareVelocities(
    private val buffers: ParticleBuffers,
) {
    private val meanSquareVelocities = KslComputeShader("MeanSquareVelocities") {
        computeStage(WORK_GROUP_SIZE) {
            val inputs = storage<KslFloat4>("velocities")
            val squareVelocities = storage<KslFloat1>("squareVelocities")

            main {
                val id = int1Var(inGlobalInvocationId.x.toInt1())
                val velocity = float3Var(inputs[id].xyz)
                val length = float1Var(length(velocity))
                squareVelocities[id] = length * length
            }
        }
    }

    private val reduce = KslComputeShader("MeanSquareVelocities_reduce") {
        computeStage(WORK_GROUP_SIZE) {
            val inputs = storage<KslFloat1>("inputs")
            val outputs = storage<KslFloat1>("outputs")
            val inputCount = uniformInt1("inputCount")
            val outputCount = uniformInt1("outputCount")

            main {
                val id = int1Var(inGlobalInvocationId.x.toInt1())
                // Sum two neighbouring values when present, keeping odd trailing elements intact
                `if`(id lt outputCount) {
                    val leftIndex = int1Var(id * 2.const)
                    val rightIndex = int1Var(leftIndex + 1.const)
                    val sum = float1Var(inputs[leftIndex])
                    `if`(rightIndex lt inputCount) {
                        sum += inputs[rightIndex]
                    }
                    outputs[id] = sum
                }
            }
        }
    }

    private val outputShader = KslComputeShader("MeanSquareVelocities_output") {
//        val struct = struct { SimulationStatisticsStruct() }
//        val total = storage("total", struct)
        val inputs = storage<KslFloat1>("inputs")
        val output = storage<KslFloat1>("outputStorage")
        computeStage(1) {
            main {
                //FIXME inGlobalInvocationId read is needed for WebGPU target, otherwise struct passed into shader inputs is not generated
                int1Var(inGlobalInvocationId.x.toInt1())
                output[0.const] = inputs[0.const]
            }
        }
    }

    private var inputs by reduce.storage("inputs")
    private var outputs by reduce.storage("outputs")
    private var inputCount by reduce.uniform1i("inputCount")
    private var outputCount by reduce.uniform1i("outputCount")

    private val inputBuffer = Buffers.floats(buffers.count)
    private val outputBuffer = Buffers.floats(buffers.count)
    val output = Buffers.floats(1)

    private val roundedUp = 1 shl (32 - (buffers.count - 1).countLeadingZeroBits())
    private val iterations = roundedUp.countTrailingZeroBits()
    private val readBack = if (iterations % 2 == 0) inputBuffer else outputBuffer

    fun addTo(
        pass: ComputePass,
    ) {
        pass.apply {
            addTask(meanSquareVelocities.apply {
                storage("velocities", buffers.velocitiesBuffer)
                storage("squareVelocities", inputBuffer)
            }, numGroups = buffers.configRepo.numGroups)
            var currentCount = buffers.count
            repeat(iterations) { iteration ->
                val nextCount = (currentCount + 1) / 2
                addTask(
                    reduce,
                    numGroups = Vec3i((nextCount + WORK_GROUP_SIZE - 1) / WORK_GROUP_SIZE, 1, 1)
                ).apply {
                    pipeline.swapPipelineData("iteration $iteration")
                    inputs = if (iteration % 2 == 0) inputBuffer else outputBuffer
                    outputs = if (iteration % 2 == 0) outputBuffer else inputBuffer
                    inputCount = currentCount
                    outputCount = nextCount
                    onBeforeDispatch {
                        pipeline.swapPipelineData("iteration $iteration")
                    }
                }
                currentCount = nextCount
            }
            addTask(outputShader.apply {
                storage("inputs").set(readBack)
                storage("outputStorage").set(output)
            }, Vec3i(1, 1, 1))
        }
    }
}

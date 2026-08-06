package me.dvyy.particles.compute.helpers

import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.*
import de.fabmax.kool.pipeline.GpuBuffer
import de.fabmax.kool.pipeline.compat.storage
import me.dvyy.particles.compute.partitioning.WORK_GROUP_SIZE

fun ResetIntsShader(
    buffer: GpuBuffer,
) = KslComputeShader("ResetBuffer") {
    computeStage(WORK_GROUP_SIZE) {
        val reset = storage<KslInt1>("reset")
        main {
            val id = int1Var(inGlobalInvocationId.x.toInt1())
            reset[id] = 0.const
        }
    }
}.apply {
    storage("reset", buffer)
}

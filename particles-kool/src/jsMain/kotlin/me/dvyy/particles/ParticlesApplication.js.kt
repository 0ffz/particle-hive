package me.dvyy.particles

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJs
import de.fabmax.kool.pipeline.backend.webgpu.RenderBackendWebGpu
import me.dvyy.particles.compute.forces.Force
import me.dvyy.particles.compute.forces.PairwiseForce
import org.koin.core.module.Module

actual fun launchParticles(
    forces: List<Force<*>>,
    wallForce: PairwiseForce,
    args: Array<String>,
    uiModule: () -> Module,
) = KoolApplication(
    config = KoolConfigJs(
        canvasName = "glCanvas",
        renderBackend = RenderBackendWebGpu,
        isGlobalKeyEventGrabbing = true,
        forceFloatDepthBuffer = false,
        deviceScaleLimit = 1.5,
    )
) {
    launchApp(ctx, forces, wallForce)
}


package me.dvyy.particles

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.pipeline.backend.gl.RenderBackendGl
import me.dvyy.particles.compute.forces.Force
import me.dvyy.particles.compute.forces.PairwiseForce
import org.koin.core.module.Module

actual fun launchParticles(forces: List<Force<*>>, wallForce: PairwiseForce, args: Array<String>, uiModule: () -> Module) {
    KoolApplication(
        config = KoolConfigJvm(
            windowTitle = "Particle HIVE",
//            isVsync = false,
//            maxFrameRate = 500,
            renderBackend = RenderBackendGl,
            windowSize = Vec2i(1920, 1080)
        )
    ) {
        launchApp(ctx, forces, wallForce, uiModule)
    }
}

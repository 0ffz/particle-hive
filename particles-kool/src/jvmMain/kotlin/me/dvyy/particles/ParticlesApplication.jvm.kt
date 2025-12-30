package me.dvyy.particles

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.pipeline.backend.vk.RenderBackendVk
import me.dvyy.particles.compute.forces.Force

actual fun launchParticles(forces: List<Force>, args: Array<String>) {
    KoolApplication(
        config = KoolConfigJvm(
            windowTitle = "Particle HIVE",
            isVsync = false,
            maxFrameRate = 500,
            renderBackend = RenderBackendVk,
            windowSize = Vec2i(1920, 1080)
        )
    ) {
        launchApp(ctx, forces)
    }
}

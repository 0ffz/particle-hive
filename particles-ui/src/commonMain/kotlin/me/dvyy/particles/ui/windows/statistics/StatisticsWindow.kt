package me.dvyy.particles.ui.windows.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.fabmax.kool.modules.compose.composables.rendering.Text
import de.fabmax.kool.toString
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.ui.composables.Category
import me.dvyy.particles.ui.composables.Graph
import me.dvyy.particles.ui.composables.Subcategory
import me.dvyy.particles.ui.helpers.koinInject
import me.dvyy.particles.ui.windows.ParticlesViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun StatisticsWindow(
    viewModel: StatisticsViewModel = koinInject(),
    particles: ParticlesViewModel = koinInject(),
    configRepo: ConfigRepository = koinInject(),
) {
    Category("Stats") {
        val fps by viewModel.fps.collectAsState()
        val simsPs by configRepo.passesPerFrame.collectAsState()
        Text("Simulation speed: ${(simsPs * fps).toString(2)} sims/s")
    }
    Category("Graphs") {
        val mean by particles.meanSquareVelocity.collectAsState()
        Subcategory("Mean Square Velocity") {
            Text("Mean Square Velocity: " + mean.toString(2))
            Graph(particles.msqvOverTime, gatherData = {
                particles.readbackMeanSquareVelocity()
            }, refreshRate = 0.05.seconds)
        }
        Subcategory("Velocity Histogram") {
            Graph(particles.velocitiesHistogram, gatherData = {
                particles.updateVelocityHistogram()
            })
        }
    }
}

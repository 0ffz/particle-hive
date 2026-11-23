package me.dvyy.particles

import me.dvyy.particles.config.AppSettings
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.dsl.ParticlesConfig
import me.dvyy.particles.dsl.Simulation
import me.dvyy.particles.dsl.Size
import me.dvyy.particles.helpers.kool.KoolTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigRepositoryTest : KoolTest() {
    @Test
    fun `saveConfigLines updates in-memory config lines even when no file is open`() {
        val repo = ConfigRepository(AppSettings())
        assertNull(repo.currentFile.value)

        val editedYaml = """
            simulation:
              count: 256
              minGridSize: 5.0
              dT: 0.002
              size:
                width: 500
                height: 500
            particles:
              argon:
                color: ff0000
                radius: 2.0
                distribution: 1
        """.trimIndent()

        repo.saveConfigLines(editedYaml)

        assertEquals(editedYaml, repo.configLines.value)
    }

    @Test
    fun `grid cells and box size scale consistently with count`() {
        val repo = ConfigRepository(AppSettings()).apply {
            updateConfig(
                ParticlesConfig(
                    simulation = Simulation(
                        count = 1000,
                        size = Size(1000, 1000),
                        minGridSize = 5.0
                    )
                )
            )
        }

        assertEquals(960, repo.count)
        assertTrue(repo.gridCells.x > 0 && repo.gridCells.y > 0)
        assertEquals(0, repo.gridCells.z)
        assertEquals(repo.gridCells.x * repo.gridSize, repo.boxSize.x, 1e-3f)
        assertEquals(repo.gridCells.y * repo.gridSize, repo.boxSize.y, 1e-3f)
    }
}

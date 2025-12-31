package me.dvyy.particles

import de.fabmax.kool.math.Vec3i
import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.KslInt1
import de.fabmax.kool.modules.ksl.lang.times
import de.fabmax.kool.modules.ksl.lang.toInt1
import de.fabmax.kool.modules.ksl.lang.x
import de.fabmax.kool.util.Int32Buffer
import kotlinx.coroutines.test.runTest
import me.dvyy.particles.compute.partitioning.WORK_GROUP_SIZE
import me.dvyy.particles.helpers.Buffers
import me.dvyy.particles.helpers.initInt
import me.dvyy.particles.helpers.kool.KoolTest
import me.dvyy.particles.ui.nodes.execManyShaders
import me.dvyy.particles.ui.nodes.execShader
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ExecShaderTest : KoolTest() {
    @Test
    fun `should be able to read executed shader data`() = runTest {
        // arrange
        val count = 100000
        val valuesBuffer = Buffers.integers(count).initInt { 0 }
        val fillShader = KslComputeShader("Fill with values") {
            val values = storage<KslInt1>("values")
            computeStage(WORK_GROUP_SIZE) {
                main {
                    val id by inGlobalInvocationId.x.toInt1()
                    values[id] = id
                }
            }
        }
        fillShader.storage("values", valuesBuffer)

        // act
        val result = Int32Buffer(count)
        execShader(scene, fillShader, numGroups = Vec3i(count / WORK_GROUP_SIZE + 1, 1, 1)) {
            valuesBuffer.downloadData(result)
        }.await()

        // assert
        assertContentEquals(
            (0..<count).toList(),
            result.toArray().toList(),
        )
    }

    @Test
    fun `should be able to correctly read initialized buffer data`() = runTest {
        // arrange
        val count = 100000
        val valuesBuffer = Buffers.integers(count)
        valuesBuffer.initInt { it }

        val multiplyByTwoShader = KslComputeShader("Multiply by two") {
            val values = storage<KslInt1>("values")
            computeStage(WORK_GROUP_SIZE) {
                main {
                    val id by inGlobalInvocationId.x.toInt1()
                    values[id] = values[id] * 2.const
                }
            }
        }
        multiplyByTwoShader.storage("values", valuesBuffer)
        // act
        val result = Int32Buffer(count)
        execManyShaders(scene, setup = {
            it.addTask(multiplyByTwoShader, numGroups = Vec3i(count / WORK_GROUP_SIZE + 1, 1, 1))
        }) {
            valuesBuffer.downloadData(result)
        }.await()

        // assert
        assertEquals(
            (0..<count).toList().map { it * 2 },
            result.toArray().toList(),
        )
    }
}
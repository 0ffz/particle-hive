package me.dvyy.particles.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import de.fabmax.kool.modules.compose.LocalUiSurface
import de.fabmax.kool.modules.compose.composables.layout.Box
import de.fabmax.kool.modules.compose.modifiers.background
import de.fabmax.kool.modules.compose.modifiers.fillMaxWidth
import de.fabmax.kool.modules.compose.modifiers.height
import de.fabmax.kool.modules.ui2.dp
import de.fabmax.kool.util.KoolDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.dvyy.compose.mini.modifier.Modifier
import me.dvyy.particles.ui.graphing.GraphNode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun Graph(
    graph: GraphNode = remember { GraphNode(Uuid.random().toHexString()) },
    gatherData: suspend GraphNode.() -> Unit,
    refreshRate: Duration = 0.1.seconds,
    modifier: Modifier = Modifier,
) {
    val surface = LocalUiSurface.current
    LaunchedEffect(graph) {
        withContext(KoolDispatchers.Frontend) {
            while (true) {
                gatherData(graph)
                delay(refreshRate)
                surface.triggerUpdate()
            }
        }
    }
    Box(modifier.background(graph).height(400.dp).fillMaxWidth()) { }
}

package me.dvyy.particles.ui.sidebar

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import de.fabmax.kool.modules.compose.LocalColors
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.layout.Row
import de.fabmax.kool.modules.compose.modifiers.background
import de.fabmax.kool.pipeline.Texture2d
import me.dvyy.compose.mini.layout.jetpack.Arrangement
import me.dvyy.compose.mini.layout.modifiers.fillMaxHeight
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
import me.dvyy.particles.ui.sidebar.AppSizes.sidebarSize
import me.dvyy.particles.ui.windows.Window

object AppSizes {
    val sidebarSize = 32.dp
}

data class WindowUiState(
    val title: String,
    val icon: Texture2d,
    val content: @Composable () -> Unit,
)

@Composable
fun Sidebar(
    tabs: List<WindowUiState>,
    rightAligned: Boolean = false,
) {
    var state by remember { mutableStateOf(SidebarUiState(-1, 350.0)) }

    fun selectOrClose(value: Int) {
        state = if (state.selectedTab == value)
            state.copy(selectedTab = -1)
        else state.copy(selectedTab = value)
    }

    Row(
        Modifier.fillMaxHeight(),
//            .layout(if (rightAligned) ReverseRowLayout else RowLayout),
        horizontalArrangement = (if (rightAligned) Arrangement.End else Arrangement.Start)
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .width(sidebarSize)
                .background(LocalColors.current.background)
        ) {
            tabs.forEachIndexed { i, window ->
                SidebarIcon(onClick = {
                    selectOrClose(i)
                }, isSelected = state.selectedTab == i, icon = window.icon)
            }
        }
        tabs.getOrNull(state.selectedTab)?.let { window ->
            Window(
                title = window.title,
                rightAligned = rightAligned,
                onDeltaResize = { state = state.copy(windowSize = state.windowSize + it.value) },
                modifier = Modifier.width(state.windowSize.dp)
            ) {
                window.content()
            }
        }
    }
}

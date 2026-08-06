package me.dvyy.particles.ui.windows

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.fabmax.kool.input.CursorShape
import de.fabmax.kool.modules.compose.LocalColors
import de.fabmax.kool.modules.compose.composables.layout.Box
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.rendering.Text
import de.fabmax.kool.modules.compose.composables.toolkit.ScrollArea
import de.fabmax.kool.modules.compose.modifiers.background
import de.fabmax.kool.modules.ui2.Dp
import me.dvyy.compose.mini.layout.jetpack.Alignment
import me.dvyy.compose.mini.layout.jetpack.ColumnScopeInstance.align
import me.dvyy.compose.mini.layout.modifiers.fillMaxHeight
import me.dvyy.compose.mini.layout.modifiers.fillMaxWidth
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
import me.dvyy.particles.ui.composables.modifiers.hoverCursor

@Composable
fun WindowTitle(title: String) = Box(
    Modifier.fillMaxWidth()
        .padding(4.dp)
        .background(LocalColors.current.primaryVariant)
) {
    Text(title)
}

@Composable
fun Window(
    title: String,
    modifier: Modifier = Modifier,
    rightAligned: Boolean = false,
    onDeltaResize: (Dp) -> Unit = {},
    content: @Composable () -> Unit,
) = Box(Modifier.fillMaxHeight()) {

    // Content
    ScrollArea(Modifier.fillMaxHeight(), scrollPaneModifier = Modifier.fillMaxHeight()) {
        Column(
            Modifier
                .background(LocalColors.current.background)
                .fillMaxHeight()
                .then(modifier)
        ) {
            WindowTitle(title)
            content()
        }
    }

    // Resize handle
    Box(
        Modifier.fillMaxHeight()
            .width(4.dp)
            .align(if (rightAligned) Alignment.Start else Alignment.End)
            .hoverCursor(shape = CursorShape.RESIZE_E)
//            .onDrag {
//                onDeltaResize(if (rightAligned) (-it.pointer.delta.x).dp else it.pointer.delta.x.dp)
//            }
    ) {}
}


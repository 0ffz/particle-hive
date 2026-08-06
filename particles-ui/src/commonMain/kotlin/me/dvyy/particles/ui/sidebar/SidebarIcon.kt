package me.dvyy.particles.ui.sidebar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.fabmax.kool.modules.compose.composables.layout.Box
import de.fabmax.kool.modules.compose.composables.rendering.Image
import de.fabmax.kool.modules.compose.modifiers.clickable
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.util.Color
import me.dvyy.compose.mini.layout.jetpack.Alignment
import me.dvyy.compose.mini.layout.modifiers.fillMaxSize
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun SidebarIcon(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: Texture2d,
    tint: Color = WHITE,
) {
//    val background = if (isSelected)
//        RoundRectBackground(Colors.primaryVariant, 6.dp)
//    else RectBackground(Color.TRANSPARENT)
    Box(Modifier.size(AppSizes.sidebarSize).padding(2.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(0.dp) //FIXME
//                .background(background)
                .clickable(/*hoverBackground = RoundRectBackground(Color.WHITE.withAlpha(0.2f), 6.dp)*/) {
                    onClick()
                }
        ) {
            Image(
                icon,
                tint,
                modifier = Modifier.align(Alignment.Center)
            ) {}
        }
    }
}

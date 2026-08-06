package me.dvyy.particles.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.fabmax.kool.modules.compose.composables.rendering.Image
import de.fabmax.kool.modules.compose.modifiers.clickable
import de.fabmax.kool.pipeline.Texture2d
import me.dvyy.compose.mini.layout.jetpack.Alignment
import me.dvyy.compose.mini.layout.jetpack.BoxScopeInstance.align
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun IconButton(icon: Texture2d, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Image(
        icon,
        modifier = modifier.clickable(
//            hoverBackground = CircularBackground(Color.WHITE.withAlpha(0.2f))
        ) {
            onClick()
        }.padding(4.dp).align(Alignment.Center)
    ) {

    }
}

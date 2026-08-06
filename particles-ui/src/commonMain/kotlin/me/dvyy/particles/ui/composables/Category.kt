package me.dvyy.particles.ui.composables

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import de.fabmax.kool.modules.compose.LocalColors
import de.fabmax.kool.modules.compose.LocalSizes
import de.fabmax.kool.modules.compose.composables.layout.Box
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.rendering.Text
import de.fabmax.kool.modules.compose.helpers.toCompose
import de.fabmax.kool.modules.compose.modifiers.background
import de.fabmax.kool.modules.compose.modifiers.onClick
import me.dvyy.compose.mini.layout.jetpack.Alignment
import me.dvyy.compose.mini.layout.modifiers.fillMaxWidth
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.modifier.Modifier
import me.dvyy.particles.ui.app.Icons

@Composable
fun Category(
    name: String,
    desc: String? = null,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }
    val colors = LocalColors.current
    val sizes = LocalSizes.current

    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
//                .height(MinFit) //FIXME MinFit
                .background(colors.primaryVariant.withAlpha(0.2f))
                .padding(vertical = sizes.smallGap.toCompose())
                .onClick { expanded = !expanded }
        ) {
            Text(
                name, Modifier.align(Alignment.Center),
                color = colors.primary,
                font = sizes.largeText,
            )
            val icon = if (expanded) Icons.chevronUp else Icons.chevronDown
            IconButton(icon, modifier = Modifier.align(Alignment.CenterEnd), onClick = { expanded = !expanded })
        }
        if (desc != null) Text(
            "*$desc",
            softWrap = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            color = colors.onBackgroundAlpha(0.5f),
        )
        if (expanded) content()
    }
}

@Composable
fun Subcategory(
    name: String,
    content: @Composable () -> Unit,
) {
    val sizes = LocalSizes.current
    val colors = LocalColors.current
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth()
                .padding(vertical = sizes.smallGap.toCompose())
                .background(LocalColors.current.primaryVariant.withAlpha(0.1f))
        ) {

            Text(
                name,
                color = colors.primary,
                font = sizes.normalText,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        content()
    }
}

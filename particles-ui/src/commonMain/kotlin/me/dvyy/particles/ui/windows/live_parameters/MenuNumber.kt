package me.dvyy.particles.ui.windows.live_parameters

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import de.fabmax.kool.modules.compose.LocalColors
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.layout.Row
import de.fabmax.kool.modules.compose.composables.rendering.Text
import de.fabmax.kool.modules.compose.composables.toolkit.Checkbox
import de.fabmax.kool.modules.compose.composables.toolkit.DropdownButton
import de.fabmax.kool.modules.compose.composables.toolkit.DropdownMenu
import de.fabmax.kool.modules.compose.composables.toolkit.DropdownMenuItem
import de.fabmax.kool.modules.compose.modifiers.background
import me.dvyy.compose.mini.layout.jetpack.Alignment
import me.dvyy.compose.mini.layout.modifiers.fillMaxWidth
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.modifier.Modifier

@Composable
fun MenuItem(
    name: String,
    content: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(name, Modifier.fillMaxWidth().align(Alignment.CenterVertically))
        content()
    }
}

@Composable
fun MenuNumber(
    name: String,
    value: Number,
    onValueChange: (Number) -> Unit,
) {
    MenuItem(name) {
        TextInputWithTooltip(value, onValueChange = onValueChange)
    }
}

@Composable
fun MenuCheckbox(
    name: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    MenuItem(name) {
        Checkbox(value, onValueChange)
    }
}

@Composable
inline fun <reified T : Enum<T>> MenuEnum(
    name: String,
    value: T,
    noinline onValueChange: (T) -> Unit,
) {
    MenuItem(name) {
        var expanded by remember { mutableStateOf(false) }
        val colors = LocalColors.current
        // TODO combobox component in kool
        Column {
//            Text(
//                value.name.lowercase().capitalize(), Modifier
//                    .background(RoundRectBackground(colors.backgroundVariant, 4.dp))
//                    .border(RoundRectBorder(colors.primaryVariant, 4.dp, 1.dp))
//                    .padding(horizontal = 6.dp, vertical = 4.dp)
//                    .clickable { expanded = !expanded }
//            )
            DropdownButton(onClick = { expanded = !expanded }) {
                Text(value.name.lowercase().capitalize())
            }

            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                enumValues<T>().forEach { enumValue ->
                    val background = if (enumValue == value)
                        Modifier.background(LocalColors.current.primaryVariant)
                    else Modifier
                    DropdownMenuItem(
                        Modifier.fillMaxWidth().then(background),
                        onClick = { onValueChange(enumValue); expanded = false },
                    ) { Text(enumValue.name.lowercase().capitalize()) }
                }
            }
        }
    }
}

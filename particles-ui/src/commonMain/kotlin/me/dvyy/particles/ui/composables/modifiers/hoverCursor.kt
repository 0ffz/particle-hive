package me.dvyy.particles.ui.composables.modifiers

import androidx.compose.runtime.Stable
import de.fabmax.kool.input.CursorShape
import me.dvyy.compose.mini.modifier.Modifier

@Stable
fun Modifier.hoverCursor(shape: CursorShape = CursorShape.DEFAULT) = this /* FIXME hoverListener(object : Hoverable {
    override fun onEnter(ev: PointerEvent) {
        PointerInput.cursorShape = shape
    }

    override fun onHover(ev: PointerEvent) {
        PointerInput.cursorShape = shape
    }
}).onDrag {
    PointerInput.cursorShape = shape
}
*/
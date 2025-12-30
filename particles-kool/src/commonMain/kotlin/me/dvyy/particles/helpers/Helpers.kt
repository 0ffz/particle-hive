package me.dvyy.particles.helpers

import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.KoolDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

fun Scene.launch(
    scope: CoroutineScope = CoroutineScope(KoolDispatchers.Frontend),
    run: suspend CoroutineScope.() -> Unit,
) {
    onRelease {
        scope.cancel()
    }
    scope.launch { run() }
}

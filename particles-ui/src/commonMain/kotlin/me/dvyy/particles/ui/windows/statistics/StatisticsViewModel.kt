package me.dvyy.particles.ui.windows.statistics

import de.fabmax.kool.util.Time
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    val scope: CoroutineScope,
) {
    val fps = Time.frameFlow.map {
        Time.fps
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)
}

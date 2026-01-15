package me.dvyy.particles.ui.windows.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.toolkit.TextField
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.ui.graphing.ConfigViewModel
import me.dvyy.particles.ui.helpers.koinInject
import me.dvyy.particles.ui.windows.live_parameters.ForceParametersViewModel

@Composable
fun TextEditorWindow(
    configRepo: ConfigRepository = koinInject(),
    forceParametersViewModel: ForceParametersViewModel = koinInject(),
    configViewModel: ConfigViewModel = koinInject(),
) {
    val lines by configRepo.configLines.collectAsState()
    Column {
        lines.lines().forEach { line -> TextField(line, onValueChange = {}) }
    }
}
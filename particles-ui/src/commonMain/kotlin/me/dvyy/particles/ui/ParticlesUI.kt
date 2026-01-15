package me.dvyy.particles.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.fabmax.kool.modules.compose.ExperimentalKoolComposeAPI
import de.fabmax.kool.modules.compose.LocalSizes
import de.fabmax.kool.modules.compose.addComposableSurface
import de.fabmax.kool.modules.ui2.Colors
import de.fabmax.kool.modules.ui2.Sizes
import de.fabmax.kool.modules.ui2.UiScene
import de.fabmax.kool.util.MdColor
import me.dvyy.particles.config.AppSettings
import me.dvyy.particles.ui.app.Icons
import me.dvyy.particles.ui.helpers.LocalKoinScope
import me.dvyy.particles.ui.helpers.koinInject
import me.dvyy.particles.ui.sidebar.Sidebar
import me.dvyy.particles.ui.sidebar.WindowUiState
import me.dvyy.particles.ui.windows.editor.TextEditorWindow
import me.dvyy.particles.ui.windows.live_parameters.LiveParametersWindow
import me.dvyy.particles.ui.windows.project_switcher.ProjectSwitcherWindow
import me.dvyy.particles.ui.windows.statistics.StatisticsWindow
import me.dvyy.particles.ui.windows.visual_options.VisualOptionsWindow
import org.koin.core.scope.Scope

private val colors = Colors.singleColorDark(MdColor.LIGHT_BLUE).run {
    copy(background = background.withAlpha(0.9f))
}

class ParticlesUI(
    val application: Scope,
) {
    val scene = UiScene {}

    @OptIn(ExperimentalKoolComposeAPI::class)
    val surface = scene.addComposableSurface(
        colors = colors,
        sizes = Sizes.large
    ) {
        CompositionLocalProvider(
            LocalKoinScope provides application,
        ) {
            val settings = koinInject<AppSettings>()
            val scale by settings.ui.scale.collectAsState()

            CompositionLocalProvider(LocalSizes provides scale.size) {
                Sidebar(
                    listOf(
                        WindowUiState("Project Chooser", Icons.folder) { ProjectSwitcherWindow() },
                        WindowUiState("Editor", Icons.fileCode) { TextEditorWindow() },
                        WindowUiState("Live Parameters", Icons.slidersHorizontal) { LiveParametersWindow() },
                    ),
                    rightAligned = false
                )
                Sidebar(
                    listOf(
                        WindowUiState("Statistics", Icons.chartSpline) { StatisticsWindow() },
                        WindowUiState("Visual Options", Icons.eye) { VisualOptionsWindow() },
                    ),
                    rightAligned = true
                )
            }
        }
    }
}

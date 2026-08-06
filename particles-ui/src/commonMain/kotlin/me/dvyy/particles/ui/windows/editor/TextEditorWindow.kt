package me.dvyy.particles.ui.windows.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.Platform
import de.fabmax.kool.modules.compose.LocalSizes
import de.fabmax.kool.modules.compose.composables.layout.Column
import de.fabmax.kool.modules.compose.composables.layout.Row
import de.fabmax.kool.modules.compose.composables.rendering.Text
import de.fabmax.kool.modules.compose.composables.toolkit.Button
import de.fabmax.kool.modules.compose.helpers.toCompose
import me.dvyy.compose.mini.layout.modifiers.fillMaxHeight
import me.dvyy.compose.mini.layout.modifiers.fillMaxSize
import me.dvyy.compose.mini.layout.modifiers.fillMaxWidth
import me.dvyy.compose.mini.layout.modifiers.padding
import me.dvyy.compose.mini.modifier.Modifier
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.config.YamlHelpers
import me.dvyy.particles.dsl.ParticlesConfig
import me.dvyy.particles.ui.graphing.ConfigViewModel
import me.dvyy.particles.ui.helpers.koinInject
import me.dvyy.particles.ui.windows.ParticlesViewModel
import me.dvyy.particles.ui.windows.live_parameters.ForceParametersViewModel

fun decodeConfigFromText(text: List<String>) = runCatching {
    YamlHelpers.yaml.decodeFromString(ParticlesConfig.serializer(), text.joinToString("\n"))
}
@Composable
fun TextEditorWindow(
    configRepo: ConfigRepository = koinInject(),
    viewModel: ParticlesViewModel = koinInject(),
    forceParametersViewModel: ForceParametersViewModel = koinInject(),
    configViewModel: ConfigViewModel = koinInject(),
) {
    val sizes = LocalSizes.current
    val config by configRepo.config.collectAsState()
    val lines by configRepo.configLines.collectAsState()
//    val decodedConfig = configRepo.configLines.debounce(0.75.seconds).map {
//        decodeConfigFromText(it)
//    }//.asMutableState(scope, default = Result.success(ParticlesConfig()))
    Column(Modifier.padding(sizes.gap.toCompose())/*.layout(ReverseColumnLayout)*/.fillMaxSize()) {
//        val color = if (config.isSuccess) colors.backgroundVariant else (MdColor.RED tone 500).withAlpha(0.1f)
        Row(Modifier.fillMaxWidth()) {
//            val config = decodedConfig.use()
//            config.onSuccess {
            Button(onClick = {
//                    val decoded = decodeConfigFromText(lines.value)
//                    decodedConfig.set(decoded)
//                    decoded.onSuccess {
//                        configRepository.saveConfigLines(lines.value.joinToString("\n"))
//                        configRepository.updateConfig(it)
//                        textChanged.set(false)
                viewModel.restartSimulation()
//                    }
            }) {
                Text("Reload")
            }
            if (KoolSystem.platform == Platform.Javascript) {
                Button(onClick = { viewModel.saveConfigAs() }) {
                    Text("Save as")
                }
            }

//                if (textChanged.use()) {
//                    Text("(Reload required)") {
//                        modifier.alignY(AlignmentY.Center)
//                    }
//                }
//                if (KoolSystem.platform == Platform.Javascript) {
//                    Image(Icons.triangleAlert) {
//                        modifier.alignY(AlignmentY.Center).tint(MdColor.ORANGE tone 100)
//                        Tooltip(
//                            "Changes made here are not saved right away! Click 'Save as' to save your changes.",
//                            tooltipState = remember { TooltipState(delay = 0.1) }
//                        )
//                    }
//                }
//            }
//            config.onFailure {
//                Text("Failed to load config: ${it.message}") {
//                    modifier.padding(sizes.smallGap).isWrapText(true).width(Grow.Std)
//                }
//            }
        }
        Column(Modifier.fillMaxHeight()) { }
//        lines.lines().forEach { line -> TextField(line, onValueChange = {}) }
    }
}
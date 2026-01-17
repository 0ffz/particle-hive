package me.dvyy.particles.ui.windows

import com.charleskorn.kaml.YamlNode
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.Platform
import de.fabmax.kool.pipeline.MipMapping
import de.fabmax.kool.pipeline.SamplerSettings
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.util.Float32Buffer
import de.fabmax.kool.util.FrontendScope
import de.fabmax.kool.util.Int32Buffer
import de.fabmax.kool.util.KoolDispatchers
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.deprecated.openFileSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import me.dvyy.particles.SceneManager
import me.dvyy.particles.compute.ParticleBuffers
import me.dvyy.particles.compute.data.MeanSquareVelocities
import me.dvyy.particles.compute.data.VelocitiesDataShader
import me.dvyy.particles.config.AppSettings
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.config.ParameterOverrides
import me.dvyy.particles.config.YamlHelpers
import me.dvyy.particles.dsl.Simulation
import me.dvyy.particles.helpers.*
import me.dvyy.particles.ui.graphing.GraphNode
import me.dvyy.particles.ui.graphing.GraphStyle

class ParticlesViewModel(
    private val buffers: ParticleBuffers,
    private val configRepo: ConfigRepository,
    private val mutableStateScope: CoroutineScope,
    val settings: AppSettings,
    private val sceneManager: SceneManager,
    private val paramOverrides: ParameterOverrides,
    private val scope: CoroutineScope,
    private val velocitiesData: VelocitiesDataShader,
    private val meanSquareData: MeanSquareVelocities,
) {
    val plotTexture = Texture2d(
        mipMapping = MipMapping.Off,
        samplerSettings = SamplerSettings().nearest(),
        name = "plot"
    )

    val velocitiesHistogram = GraphNode("velocity-histogram").apply {
        style = GraphStyle.Bar(width = 6.0)
    }
    val msqvOverTime = GraphNode("msqv-over-time").apply {
        render(FloatArray(1024) { it.toFloat() }, FloatArray(1024) { 0f })
        style = GraphStyle.Bar(width = 3.0)
    }

//    val configUiState: Flow<ConfigUiState> = configRepo.fileUpdates
//        .debounce(1.seconds)
//        .mapLatest<PlatformFile, ConfigUiState> {
//            val config = it.readString()
//            ConfigUiState.Decoded(YamlHelpers.yaml.decodeFromString(ParticlesConfig.serializer(), config))
//        }
//        .retryWhen { cause, attempt ->
//            emit(ConfigUiState.Error(cause))
//            true
//        }
//        .stateIn(scope, SharingStarted.Lazily, ConfigUiState.Loading)

    val meanSquareVelocity = MutableStateFlow(0f)

    suspend fun updateVelocityHistogram() = withContext(KoolDispatchers.Backend) {
        val buckets = Int32Buffer(velocitiesData.numBuckets)
        velocitiesData.buckets.downloadData(buckets)
        val bucketsArray = buckets.toArray()
        velocitiesHistogram.render(
            FloatArray(bucketsArray.size) { it.toFloat() },
            bucketsArray.map { it.toFloat() }.toFloatArray()
        )
    }

    suspend fun readbackMeanSquareVelocity() = withContext(KoolDispatchers.Synced) {
        val result = Float32Buffer(1)
        meanSquareData.output.downloadData(result)
        val msqV = result[0] / buffers.count
        msqvOverTime.pushNewValueRight(msqV)
        meanSquareVelocity.update { msqV }
    }

    fun updateState(simulation: Simulation.() -> Simulation) = scope.launch {
        val config = configRepo.config.value
        val newSimulation = simulation(config.simulation)
        configRepo.updateConfig(config.copy(simulation = newSimulation))
    }

    fun <T> updateOverrides(key: String, newValue: T, serializer: KSerializer<T>) = scope.launch {
        paramOverrides.update(
            key, YamlHelpers.yaml.decodeFromString(
                YamlNode.serializer(),
                YamlHelpers.yaml.encodeToString(serializer, newValue)
            )
        )
    }

    fun resetPositions() = scope.launch {
        buffers.positionBuffer.initFloat4 {
            Buffers.randomPosition(configRepo.boxSize)
        }
        buffers.initializeParticlesBuffer()
    }

    fun restartSimulation() = FrontendScope.launch { // scope will get cancelled during reload, so we use main thread
        sceneManager.reload()
    }

    fun resetParameters() = FrontendScope.launch {
        paramOverrides.reset()
    }

    fun attemptOpenProject() = FrontendScope.launch {
        val file = FilePicker.pickFile("yml") ?: return@launch
        println("Opening scene...")
        sceneManager.open(file)
        println("Opened scene")
        val path = file.path
        println("Saving $path to recent projects")
        settings.recentProjectPaths.update { (listOf(path.toString()) + it).distinct() }
    }

    fun openProject(path: ConfigPath) = FrontendScope.launch {
        sceneManager.open(path.readContents() ?: return@launch)
        settings.recentProjectPaths.update { listOf(path.toString()) + (it - path.toString()) }
    }

    fun removeProject(path: ConfigPath) = FrontendScope.launch {
        settings.recentProjectPaths.update { it - path.toString() }
        if (KoolSystem.platform == Platform.Javascript) {
            FileSystemUtils.clearCachedFileIfExists(path)
        }
    }

    fun saveConfigAs() = FrontendScope.launch {
        FileSystemUtils.saveFileAs(
            configRepo.configLines.value.encodeToByteArray(),
            configRepo.currentFile.value?.name ?: "config.yml"
        )
    }

    fun saveClusterData() {
        FrontendScope.launch {
            val info = buffers.clusterInfo?.sizes ?: return@launch
            FileKit.openFileSaver(info.joinToString("\n").encodeToByteArray(), "data", "csv")
        }
    }
}

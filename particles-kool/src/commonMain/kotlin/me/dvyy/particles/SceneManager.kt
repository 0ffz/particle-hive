package me.dvyy.particles

import de.fabmax.kool.KoolContext
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.KoolDispatchers
import de.fabmax.kool.util.SyncedScope
import de.fabmax.kool.util.delayFrames
import kotlinx.coroutines.*
import me.dvyy.particles.compute.forces.Force
import me.dvyy.particles.compute.forces.ForceBindings
import me.dvyy.particles.compute.forces.PairwiseForce
import me.dvyy.particles.config.AppSettings
import me.dvyy.particles.config.ConfigRepository
import me.dvyy.particles.helpers.ConfigPath
import me.dvyy.particles.helpers.FilePickerResult
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class SceneManager(
    val ctx: KoolContext,
    /** Classes/data that persists across application reloads. */
    val baseModule: Module,
    val forces: List<Force<*>>,
    val wallForce: PairwiseForce,
    val uiModule: () -> Module,
) {
    private var loadedScenes: List<Scene> = listOf()
    val mainScene get() = loadedScenes.first()
    val globalApplication = koinApplication { modules(baseModule) }

    suspend fun reload() = withContext(KoolDispatchers.Synced) {
        unload()
        load()
    }

    fun load() = SyncedScope.launch {
        val sceneScope = CoroutineScope(KoolDispatchers.Synced)
        // Create dependencies with koin
        val application = koinApplication {
            modules(
                module {
                    single { sceneScope }
                    single<SceneManager> { this@SceneManager }
                },
                baseModule,
                module {
                    single { ForceBindings(wallForce, forces, get<ConfigRepository>().config.value) }
                },
                dataModule(),
                shadersModule(),
                sceneModule(),
                uiModule(),
            )
        }.koin
        val configRepo = application.get<ConfigRepository>()
        val settings = application.get<AppSettings>()
        configRepo.isDirty = true
        val scene = application.get<ParticlesScene>().scene
        val ui = application.get<Scene>(named("ui-scene"))

        scene.onRelease { sceneScope.cancel() }
        ctx.scenes.stageAdd(scene, index = 0)
        ctx.scenes += ui
        loadedScenes = listOf(scene, ui)
        if (configRepo.currentFile.value == null) {
            val lastOpened = settings.recentProjectPaths.value.firstOrNull()
                ?.let { ConfigPath(it).readContents() }

            if (lastOpened != null) open(lastOpened)
        }
    }

    suspend fun unload() = withContext(KoolDispatchers.Synced) {
        loadedScenes.forEach { scene ->
            ctx.removeScene(scene)
        }
        delayFrames(1)
        loadedScenes.forEach { scene ->
            scene.release()
        }
        loadedScenes = listOf()
        delayFrames(1)
    }

    suspend fun open(file: FilePickerResult): Job = withContext(KoolDispatchers.Synced) {
        val config = globalApplication.koin.get<ConfigRepository>()
        config.openFile(file)
        reload()
    }
}

package me.dvyy.particles.dsl.debug

import com.mineinabyss.jsonschema.annotations.Description
import kotlinx.serialization.Serializable

@Serializable
class DebugOptions(
    //TODO
    @Description("Set log level (currently not implemented)")
    val logLevel: String = "info",
    @Description("Enable or disable shaders in the pipeline.")
    val shaders: ShaderOptions = ShaderOptions(),
)

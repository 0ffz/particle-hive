package me.dvyy.particles.compute.forces

import de.fabmax.kool.modules.ksl.lang.KslComputeStage
import me.dvyy.particles.compute.forces.builders.FunctionParameter

data class Force<T>(
    val name: String,
    val type: String,
    val parameters: List<FunctionParameter<*>>,
    val createFunction: KslComputeStage.() -> T,
) {
    fun parseParameters(config: Map<String, Float>): FloatArray = parameters
        .map { config[it.name] ?: error("Missing parameter ${it.name}") }
        .toFloatArray()
}

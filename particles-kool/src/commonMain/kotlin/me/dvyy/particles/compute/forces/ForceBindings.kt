package me.dvyy.particles.compute.forces

import me.dvyy.particles.dsl.ParticlesConfig
import me.dvyy.particles.dsl.pairwise.ParticleSet

class ForceBindings(
    val wallForce: PairwiseForce,
    val forceTypes: List<Force<*>>,
    private val config: ParticlesConfig,
) {
    val particleTypeCount = config.particles.size
    val forces: List<ForceParameterBinding<*>> = config.interactions.map { (name, params) ->
        val force = forceTypes.find { it.name == name } ?: error("Unknown force: $name")
        ForceParameterBinding(force, particleTypeCount).apply {
            params.forEach { (key, values) ->
                val set = with(config) { ParticleSet.fromString(key) }
                put(set, force.parseParameters(values))
            }
        }
    }

    val pairwiseForces: List<ForceParameterBinding<PairwiseForce>> = forces
        .filter { it.force.type == "pairwise" } as List<ForceParameterBinding<PairwiseForce>>

    val individualForces: List<ForceParameterBinding<IndividualForce>> = forces
        .filter { it.force.type == "individual" } as List<ForceParameterBinding<IndividualForce>>
}

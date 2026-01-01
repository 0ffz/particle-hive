package me.dvyy.particles.dsl

import com.mineinabyss.jsonschema.annotations.Description
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal typealias Parameters = Map<String, Float>
internal typealias InteractionName = String
internal typealias PairName = String

@Serializable
data class ParticlesConfig(
    @Description("Parameters for the simulation.")
    val simulation: Simulation = Simulation(),
    @Description("Parameters for the application frontend interface.")
    val application: ApplicationConfiguration = ApplicationConfiguration(),
    @Description("Particle type definitions (a map of particle name to its definition)")
    @SerialName("particles")
    val nameToParticle: Map<String, Particle> = mapOf(),
    @Description("""Particle interaction definitions (forces acting on particles).
For individual forces, use the particle name as the key.
For pairwise forces, use 'typeA-typeB' pairs as the key.""")
    val interactions: Map<InteractionName, Map<PairName, Parameters>> = mapOf(),
) {
    @Transient
    val particles = nameToParticle.values.toList()

    val particleTypeCount = particles.size

    @Transient
    private var highestId = 0

    @Transient
    val particleIds: Map<String, ParticleId> = nameToParticle.mapValues { ParticleId(highestId++) }

    fun particle(name: String): ParticleId = particleIds[name] ?: error("Particle with name $name not found")

    fun particleName(id: ParticleId) = particleIds.entries.firstOrNull { it.value == id }?.key ?: error("Particle with id $id not found")
//    @Transient
//    val pairwiseInteractions: Map<InteractionName, Map<ParticlePair, Parameters>> = interactions.mapNotNull { (name, interactions) ->
//        val pair = ParticlePair.fromString(name, particleIds) ?: return@mapNotNull null
//        pair to interactions
//    }.toMap()
//
//    @Transient
//    val individualInteractions: Map<ParticleId, Map<PairName, Parameters>> = interactions.mapNotNull { (name, interactions) ->
//        val individual = particleIds[name] ?: return@mapNotNull null
//        individual to interactions
//    }.toMap()
}

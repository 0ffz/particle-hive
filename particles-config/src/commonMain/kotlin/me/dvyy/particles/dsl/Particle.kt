package me.dvyy.particles.dsl

import com.mineinabyss.jsonschema.annotations.Description
import kotlinx.serialization.Serializable

@Serializable
data class Particle(
    @Description("The color of the particle as a hex code (ex. ff0000 for red).")
    val color: String = "ffffff",
    @Description("The radius of the particle when displayed on screen. Does not affect the simulation itself.")
    val radius: Double = 5.0,
    @Description(
        "When randomly assigning particle types, how much to assign these particles as a ratio relative to all others. " +
                "(ex. if type A has distribution 1, and B has 2, B will be twice as common as A.)"
    )
    val distribution: Double = 1.0,
    @Description("Options for converting this particle type to another during runtime. Useful for slowly introducing new particles.")
    val convertTo: Conversion? = null,
)

@Serializable
data class Conversion(
    @Description("The particle type to convert to.")
    val type: String,
    @Description("The chance any given particle of this type will be converted to the target type every [conversionRate] timesteps.")
    val chance: Double,
)

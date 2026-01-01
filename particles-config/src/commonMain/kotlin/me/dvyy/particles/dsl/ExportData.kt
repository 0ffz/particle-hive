package me.dvyy.particles.dsl

import com.mineinabyss.jsonschema.annotations.Description
import kotlinx.serialization.Serializable


@Serializable
data class ExportData(
    @Description("The type of data to export per particle. This can be displayed by switching the 'Particle color' setting to exported data.")
    val type: ExportDataType = ExportDataType.LOCAL_NEIGHBOURS,
    @Description("What to multiply the collected value by such that 0 is the minimum and 1 is the maximum visible value.")
    val rescale: Double = 1.0,
)

@Serializable
enum class ExportDataType {
    LOCAL_NEIGHBOURS, CELL_PARTICLE_COUNT,
}

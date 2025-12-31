package me.dvyy.particles.dsl

import kotlinx.serialization.Serializable


@Serializable
data class ExportData(
    val type: ExportDataType = ExportDataType.LOCAL_NEIGHBOURS,
    val rescale: Double = 1.0,
)

@Serializable
enum class ExportDataType {
    LOCAL_NEIGHBOURS, CELL_PARTICLE_COUNT,
}
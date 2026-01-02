package me.dvyy.particles.dsl.debug

import kotlinx.serialization.Serializable

@Serializable
class ShaderOptions(
    val particleMesh: Boolean = true,
    val resetBuffers: Boolean = true,
    val sort: Boolean = true,
    val reorderBuffers: Boolean = true,
    val calculateOffsets: Boolean = true,
    val fields: Boolean = true,
    val convert: Boolean = true,
    val velocityData: Boolean = true,
    val meanSquareData: Boolean = true,
)

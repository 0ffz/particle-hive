package me.dvyy.particles.compute.simulation

import de.fabmax.kool.util.MemoryLayout
import de.fabmax.kool.util.Struct

object SimulationParametersStruct : Struct("SimulationParametersStruct", MemoryLayout.Std140) {
    val maxVelocity = float1("maxVelocity")
    val maxForce = float1("maxForce")
    val targetVelocity = float1("targetVelocity")
    val targetVelocityFixStrength = float1("targetVelocityFixStrength")
    val exportDataRescale = float1("exportDataRescale")
    val exportDataType = int1("exportDataType")
}

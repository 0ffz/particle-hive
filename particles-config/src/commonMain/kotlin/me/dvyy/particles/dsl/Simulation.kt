package me.dvyy.particles.dsl

import com.mineinabyss.jsonschema.annotations.Description
import kotlinx.serialization.Serializable

@Serializable
data class Simulation(
    @Description("Number of particles to simulate, rounded to nearest multiple of 64")
    val count: Int = 10_000,
    @Description("How often to run conversions in steps (if specified)")
    val conversionRate: Int = 100,
    @Description("Minimum size of grid cells, should be at least as big as the longest-range interaction possible between two particles")
    val minGridSize: Double = 5.0,
    @Description("Time step size (larger steps update velocity and position of particles at larger increments)")
    val dT: Double = 0.001,
    @Description("Maximum length the velocity vector of a particle can have.")
    val maxVelocity: Double = 20.0,
    @Description("Maximum length the force vector of a particle can have.")
    val maxForce: Double = 100000.0,
    @Description("Target velocity to aim for by nudging particle velocities slightly based on their median.")
    val targetVelocity: Double = 0.0,
    @Description("How strongly to nudge particles towards the target velocity.")
    val targetVelocityStrength: Double = 0.0,
    @Description("Options for exporting calculated particle data for visualization (or in the future other types of collection)")
    val exportData: ExportData = ExportData(),
    @Description("Should the simulation run in 3D space?")
    val threeDimensions: Boolean = false,
    @Description(
        """How many iterations of the simulation to run per *rendered* frame.
Particles are only re-sorted into cells after the last pass,
thus higher numbers lose some accuracy as particles move to different cells.""",
    )
    val passesPerFrame: Int = 100,
    @Description("Size of the simulation box, rounded to the nearest grid size.")
    val size: Size = Size(),
)

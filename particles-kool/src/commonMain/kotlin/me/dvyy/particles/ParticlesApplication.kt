package me.dvyy.particles

import me.dvyy.particles.compute.forces.Force
import me.dvyy.particles.compute.forces.PairwiseForce

expect fun launchParticles(forces: List<Force<*>>, wallForce: PairwiseForce, args: Array<String>)

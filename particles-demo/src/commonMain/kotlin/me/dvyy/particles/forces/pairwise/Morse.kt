package me.dvyy.particles.forces.pairwise

import de.fabmax.kool.modules.ksl.lang.minus
import de.fabmax.kool.modules.ksl.lang.times
import de.fabmax.kool.modules.ksl.lang.unaryMinus
import me.dvyy.particles.compute.forces.buildForce

/**
 * Derivative of the [Morse potential](https://en.wikipedia.org/wiki/Morse_potential#Potential_energy_function).
 */
val Morse = buildForce("morse") {
    val De = paramFloat("De", "Well depth")
    val a = paramFloat("a", "Well 'width'")
    val re = paramFloat("re", "Equilibrium bond distance")
    pairwise { distance, localCount ->
        val De = De()
        val a = a()
        val re = re()

        body {
            val exponential = float1Var(exp(-a * (distance - re)), "exponential")
            (-2f).const * a * De * exponential * (1f.const - exponential)
        }
    }
}
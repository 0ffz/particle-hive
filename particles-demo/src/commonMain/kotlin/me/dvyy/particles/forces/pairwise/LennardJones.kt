package me.dvyy.particles.forces.pairwise

import de.fabmax.kool.modules.ksl.lang.div
import de.fabmax.kool.modules.ksl.lang.minus
import de.fabmax.kool.modules.ksl.lang.times
import me.dvyy.particles.compute.forces.buildForce

/**
 * Derivative of the [Lennard Jones potential](https://en.wikipedia.org/wiki/Lennard-Jones_potential).
 */
val LennardJones = buildForce("lennardJones") {
    val sigma = paramFloat("sigma")
    val epsilon = paramFloat("epsilon")
    pairwise { distance, localCount ->
        val sigma = sigma()
        val epsilon = epsilon()

        body {
            val invR = float1Var(sigma / distance, "invR")
            val invR6 = float1Var(invR * invR * invR * invR * invR * invR, "invR6")
            val invR12 = float1Var(invR6 * invR6, "invR12")
            24f.const * epsilon * (2f.const * invR12 - invR6) / distance
        }
    }
}

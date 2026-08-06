package me.dvyy.particles.helpers

import de.fabmax.kool.modules.ksl.lang.sqrt
import de.fabmax.kool.modules.ksl.lang.times
import me.dvyy.particles.compute.forces.buildForce

internal val TestForce = buildForce("test_force") {
    val scalar = paramFloat("scalar")

    pairwise { distance, localCount ->
        body {
            scalar() * sqrt(distance)
        }
    }
}

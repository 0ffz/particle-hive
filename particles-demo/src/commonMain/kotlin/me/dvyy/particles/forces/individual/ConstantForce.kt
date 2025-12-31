package me.dvyy.particles.forces.individual

import me.dvyy.particles.compute.forces.buildForce

/**
 * A constant downwards force.
 */
val ConstantForce = buildForce("gravity") {
    val force = paramFloat("force")

    individual {
        body {
            float3Value(0f.const, force(), 0f.const)
        }
    }
}

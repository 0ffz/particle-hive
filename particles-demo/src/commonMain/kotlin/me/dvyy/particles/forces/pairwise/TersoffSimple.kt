package me.dvyy.particles.forces.pairwise

import de.fabmax.kool.math.PI_F
import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.forces.buildForce

object TersoffSimple {
    val force = buildForce("tersoff_simple") {
        val A = paramFloat("A")
        val B = paramFloat("B")
        val lambda1 = paramFloat("lambda1")
        val lambda2 = paramFloat("lambda2")
        val beta = paramFloat("beta")
        val n = paramFloat("n")

        pairwise { distance, localCount ->
            val A = A()
            val B = B()
            val lambda1 = lambda1()
            val lambda2 = lambda2()
            val beta = beta()
            val n = n()

            body {
                // assuming: cutoff is just 1, b_ij is constant
                val cutoff = float1Var(cutoff(distance, 0.2f.const, 3f.const), "cutoff")
                val d_cutoff = float1Var(d_cutoff(distance, 0.2f.const, 3f.const), "dCutoff")
                val repulsive = float1Var(force(A, lambda1, distance), "repulsive")
                val attractive = float1Var(force(-B, lambda2, distance), "attractive")
                val d_repulsive = float1Var(d_force(A, lambda1, distance), "dRepulsive")
                val d_attractive = float1Var(d_force(-B, lambda2, distance), "dAttractive")
                val b_ij = float1Var(b_ij(localCount, beta, n), "bIJ")
                val `d(b_ij)` = float1Var(`d(b_ij)`(localCount, beta, n), "dBIJ")
                val normalTerm = repulsive + b_ij * attractive
                val derivativeTerm = d_repulsive + (`d(b_ij)` * attractive) + (b_ij * d_attractive)

                return@body -(d_cutoff * normalTerm + cutoff * derivativeTerm)
            }
        }
    }
//    val cutoffR = param<Float>("cutoffStart")
//    val cutoffD = param<Float>("cutoffEnd")

    //    val KslComputeStage.cutoffFunction
//        get() = functionFloat1("tersoff_cutoff") {
//            paramFloat1("distance")
//            body {
//                1f.const //TODO implement
//            }
//        }
//    forceBetweenParticles += -tersoff(
//    localCount = localCount,
//    distance = dist,
//    beta = ,
//    n = ,
//    A = ,
//    B = ,
//    lambda_1 = ,
//    lambda_2 = ,
//    )

    fun KslScopeBuilder.cutoff(
        distance: KslExprFloat1,
        cutoffD: KslExprFloat1,
        cutoffR: KslExprFloat1,
    ): KslExprFloat1 {
        // Clamp the distance to the transition range [lowerBound, upperBound].
        val clampedDistance = clamp(distance, cutoffR - cutoffD, cutoffR + cutoffD)
        return 0.5f.const - (0.5f.const * sin((PI_F / 2f).const * (clampedDistance - cutoffR) / cutoffD))
    }

    fun KslScopeBuilder.`d_cutoff`(
        distance: KslExprFloat1,
        cutoffD: KslExprFloat1,
        cutoffR: KslExprFloat1,
    ): KslExprFloat1 {
        // Define the lower and upper bounds of the transition region.
        val lowerBound = cutoffR - cutoffD
        val upperBound = cutoffR + cutoffD

        // Calculate the core derivative expression.
        // d/dx [ 0.5 - 0.5 * sin(k * (x - R)) ] = -0.5 * k * cos(k * (x - R))
        // where k = PI / (2 * D)
        val cosTerm = cos((PI_F / 2f).const * (distance - cutoffR) / cutoffD)
        val derivative = -((PI_F / 4f).const / cutoffD) * cosTerm

        // Create a factor that is 1.0 inside the transition range and 0.0 outside.
        // This is a branchless way to implement the condition.
        // step(a, x) is 0 if x < a, and 1 if x >= a.
        // The factor is 1.0 only when distance >= lowerBound AND distance < upperBound.
        val insideRangeFactor = step(lowerBound, distance) * (1f.const - step(upperBound, distance))

        // Multiply the derivative by the factor to correctly zero it out outside the range.
        return derivative * insideRangeFactor
    }

    // We assume a_ij = 1, as suggested by Tersoff

    fun KslScopeBuilder.`b_ij`(
        localCount: KslExprFloat1,
        beta: KslExprFloat1,
        n: KslExprFloat1,
    ) = pow(1f.const + pow(beta, n) * pow(localCount, n), ((-1f).const / (2f.const * n)))

    fun KslScopeBuilder.`d(b_ij)`(
        localCount: KslExprFloat1,
        beta: KslExprFloat1,
        n: KslExprFloat1,
    ): KslExpressionMathScalar<KslFloat1> {
        val beta_n = float1Var(pow(beta, n), "betaN")
        return -(beta_n / 2f.const) * pow(
            1f.const + beta_n * pow(localCount, n),
            -(1f.const + 2f.const * n) / (2f.const * n)
        ) * pow(localCount, n - 1f.const)
    }

    fun KslScopeBuilder.force(
        scalar: KslExprFloat1,
        lambda: KslExprFloat1,
        distance: KslExprFloat1,
    ) = scalar * exp(-lambda * distance)

    fun KslScopeBuilder.d_force(
        scalar: KslExprFloat1,
        lambda: KslExprFloat1,
        distance: KslExprFloat1,
    ) = -(scalar * lambda) * exp(-lambda * distance)
}

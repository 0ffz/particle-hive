package me.dvyy.particles.compute.forces

import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.helpers.KslInt

data class IndividualForceFunction(
    private val function: KslFunctionFloat3,
) {
    context(scope: KslScopeBuilder)
    operator fun invoke(
        position: KslExprFloat3,
        parameters: ParametersArray<IndividualForce>,
    ): KslExprFloat3 = with(scope) {
        function.invoke(position, *parameters)
    }
}

typealias IndividualForce = Force<IndividualForceFunction>

/**
 * A force between two particles, given only distance as a parameter
 */
data class PairwiseForceFunction(
    private val function: KslFunctionFloat1,
) {
    context(scope: KslScopeBuilder)
    operator fun invoke(
        distance: KslExprFloat1,
        localCount: KslExprFloat1,
        parameters: ParametersArray<PairwiseForce>,
    ): KslExprFloat1 = with(scope) {
        function.invoke(distance, localCount, *parameters)
    }

    companion object {
        /** Gets the hash for a pair of particle types (symmetrical), knowing the total particle type count. */
        context(scope: KslScopeBuilder)
        fun pairHash(first: KslInt, second: KslInt, totalParticleTypes: Int): KslInt = with(scope) {
            val min = min(first, second)
            val max = max(first, second)
            return min + max * totalParticleTypes.const
        }
    }
}

typealias PairwiseForce = Force<PairwiseForceFunction>

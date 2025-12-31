package me.dvyy.particles.compute.forces

import de.fabmax.kool.modules.ksl.lang.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.dvyy.particles.compute.forces.builders.FunctionParameter
import kotlin.jvm.JvmName

inline fun <T> buildForce(identifier: String, block: ForceBuilder.() -> Force<T>): Force<T> {
    return ForceBuilder(identifier).run(block)
}

class ForceBuilder(val name: String) {
    @PublishedApi
    internal val parameters = mutableListOf<FunctionParameter<*>>()

    private inline fun <reified T> addParam(
        name: String,
        serializer: KSerializer<T> = serializer<T>(),
    ): FunctionParameter<T> {
        val param = FunctionParameter<T>(name, serializer)
        parameters += param
        return param
    }

    //    context(function: KslFunction<*>)
    fun paramFloat(name: String, description: String? = null): FunctionParameter<Float> {
        return addParam<Float>(name)
//        return function.paramFloat1(name)
    }

    fun paramInt(name: String, description: String? = null): FunctionParameter<Int> {
        return addParam<Int>(name)
//        return function.paramInt1(name)
    }

    @JvmName("paramInt1")
    context(function: KslFunction<*>)
    operator fun FunctionParameter<Int>.invoke() = function.paramInt1(name)

    @JvmName("paramFloat1")
    context(function: KslFunction<*>)
    operator fun FunctionParameter<Float>.invoke() = function.paramFloat1(name)

    fun individual(
        create: context(KslFunctionFloat3) (position: KslExprFloat3) -> Unit,
    ): IndividualForce {
        return build("individual") {
            IndividualForceFunction(
                functionFloat3(name) {
                    val position = paramFloat3("position")
                    create(position)
                }
            )
        }
    }

    fun pairwise(
        create: context(KslFunctionFloat1) (distance: KslExprFloat1, localCount: KslExprFloat1) -> Unit,
    ): Force<PairwiseForceFunction> {
        return build("pairwise") {
            PairwiseForceFunction(
                functionFloat1(name) {
                    val distance = paramFloat1("dist")
                    val localCount = paramFloat1("localCount")
                    create(distance, localCount)
                }
            )
        }
    }

    // Expose only `body` to builders to prevent incorrect parameter binding
    context(function: KslFunction<T>)
    fun <T : KslType> body(block: KslScopeBuilder.() -> KslExpression<T>) {
        function.body { block() }
    }


    inline fun <T> build(type: String, crossinline create: KslComputeStage.() -> T): Force<T> =
        Force(name, type, parameters.toList()) {
            create()
        }
}
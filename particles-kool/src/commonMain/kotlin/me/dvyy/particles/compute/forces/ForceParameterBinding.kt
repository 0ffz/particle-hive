package me.dvyy.particles.compute.forces

import de.fabmax.kool.KoolSystem
import de.fabmax.kool.Platform
import de.fabmax.kool.modules.ksl.KslComputeShader
import de.fabmax.kool.modules.ksl.lang.*
import de.fabmax.kool.pipeline.ComputeShader
import de.fabmax.kool.util.MemoryLayout
import de.fabmax.kool.util.Struct
import de.fabmax.kool.util.set
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.dvyy.particles.compute.helpers.KslInt
import me.dvyy.particles.dsl.pairwise.ParticleSet


typealias ParametersArray<T> = Array<KslStructMemberExpressionScalar<KslFloat1, ForceParameterBinding<T>.InteractionStruct>>

class KslForce<R, T : Force<R>>(
    val binding: ForceParameterBinding<T>,
    private val stage: KslComputeStage,
) {
    val function: R = binding.force.createFunction(stage)

    /** Creates a UBO representing the parameters */
    val forceParameters = stage.program.uniformStruct(binding.uniformName, binding.forceParametersStruct)

    fun interactionFor(hash: KslInt) = forceParameters[binding.forceParametersStruct.interactions][hash]

    fun KslVarStruct<ForceParameterBinding<T>.InteractionStruct>.parametersAsArray(): ParametersArray<T> =
        binding.interactionsStruct.parameters.map { this[it] }.toTypedArray()

}

class ForceParameterBinding<T : Force<*>>(
    val force: T,
    private val totalParticles: Int,
) {
    private val parameterMatrices = mutableMapOf<ParticleSet, FloatArray>()
    private val numParameters = force.parameters.size

    val interactionsStruct = InteractionStruct()
    val forceParametersStruct = ForceParametersStruct()

    val hashCount = when (force.type) {
        "pairwise" -> totalParticles * totalParticles
        "individual" -> totalParticles
        else -> error("Invalid force type")
    }

    val uniformName = "${force.name}_parameters"
    val parameterNames = force.parameters.map { it.name }

    private val _changes = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    ).apply { tryEmit(Unit) }
    val changes = _changes.asSharedFlow()

    fun put(set: ParticleSet, values: FloatArray) {
        parameterMatrices[set] = values
        _changes.tryEmit(Unit)
    }

    fun update(set: ParticleSet, param: String, value: Float) {
        put(set, get(set)!!.apply { set(parameterNames.indexOf(param), value) })
    }

    fun get(pair: ParticleSet): FloatArray? = parameterMatrices[pair]

    fun getAll(): Map<ParticleSet, FloatArray> = parameterMatrices

    /** Updates bound UBO with parameters on CPU. */
    context(shader: ComputeShader)
    fun uploadParameters() {
        val ubo = shader.uniformStruct(uniformName, forceParametersStruct)
        // Clear all data (zero matrix represents skipping parameters)
        ubo.set {
            repeat(it.interactions.arraySize) { i ->
                set(it.interactions, i) { interaction ->
                    interaction.enabled.set(false)
                    interaction.parameters.forEach { it.set(0f) }
                }
            }
            parameterMatrices.forEach { (set, params) ->
                set(it.interactions, set.hash) {
                    it.enabled.set(true)
                    it.parameters.forEachIndexed { i, param -> param.set(params[i]) }
                }
            }
        }
    }

    fun createPairwiseForceComputeShader() = KslComputeShader("force-one-shot") {
        TODO()
//        computeStage(WORK_GROUP_SIZE) {
//            val localNeighbors = uniformFloat1("localNeighbors")
//            val lastIndex = uniformInt1("lastIndex")
//            val distances = storage<KslFloat1>("distances")
//            val output = storage<KslFloat1>("outputBuffer")
//            forceParameters
//
//            val function = (force as PairwiseForce).createFunction(this)
//            main {
//                val id = int1Var(inGlobalInvocationId.x.toInt1())
//                `if`(id le lastIndex) {
//                    val extractedParams = structVar(interactionFor(0.const))
//                    output[id] = function.invoke(
//                        distance = distances[id],
//                        localCount = localNeighbors,
//                        parameters = extractedParams.parametersAsArray() as ParametersArray<PairwiseForce>,
//                    )
//                }
//            }
//        }
    }

    inner class InteractionStruct : Struct("InteractionStruct_${force.name}", MemoryLayout.Std140) {
        val enabled = bool1("enabled")
        val parameters = (0..<numParameters).map { float1() }

        init {
            // WebGPU requires a multiple of 16 bytes for uniforms
            // Add padding to make the struct size a multiple of 32 bytes
            // Size so far: 4 (enabled) + numParameters * 4 bytes
            if (KoolSystem.platform == Platform.Javascript) {
                val currentSize = 4 + numParameters * 4 // 4 bytes per float parameter
                val remainder = currentSize % 16
                if (remainder != 0) {
                    val paddingSize = 16 - remainder
                    val numPaddingElements = paddingSize / 4
                    repeat(numPaddingElements) {
                        float1() // Add padding elements
                    }
                }
            }
        }
    }

    inner class ForceParametersStruct : Struct("ForceParametersStruct_${force.name}", MemoryLayout.Std140) {
        val interactions = structArray(interactionsStruct, hashCount.coerceAtLeast(1), "interactions")
    }
}

internal operator fun KslMatrix4Accessor.get(index: Int) = when (index) {
    0 -> x
    1 -> y
    2 -> z
    3 -> w
    else -> error("Invalid index $index for matrix")
}

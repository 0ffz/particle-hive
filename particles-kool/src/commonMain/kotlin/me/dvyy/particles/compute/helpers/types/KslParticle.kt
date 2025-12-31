package me.dvyy.particles.compute.helpers.types

import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.simulation.FieldsShaderProgram

/**
 * Helper for reading properties given a particle id, each variable inside will lazily be added to the scope
 * it was created in as requested. The generated shader will look like variable declarations within [scope]
 * that are calculated once.
 */
class KslParticle(
    val id: KslExprInt1,
    varName: String? = null,
    private val program: FieldsShaderProgram,
    private val scope: KslScopeBuilder,
) {
    val position by lazyScope(scope) { float3Var(program.positions[id].xyz, varName?.let { "${it}_position" }) } //p(t + dt); since half step runs before this
    val velocity by lazyScope(scope) { float3Var(program.velocities[id].xyz, varName?.let { "${it}_velocity" }) } //v(t + dt/2)
    val currForce by lazyScope(scope) { float3Var(program.forces[id].xyz, varName?.let { "${it}_currForce" }) }
    val type by lazyScope(scope) { int1Var(program.particleTypes[id], varName?.let { "${it}_type" }) }

    // Compute grid indices based on the particle position
    val cell by lazyScope(scope) { int3Var((position / program.gridSize).toInt3(), varName?.let { "${it}_cell" }) }
}

context(program: FieldsShaderProgram, scope: KslScopeBuilder)
fun particle(id: KslExprInt1, varName: String? = null) = KslParticle(id, varName, program, scope)

fun <T> lazyScope(scope: KslScopeBuilder, initializer: KslScopeBuilder.() -> T) = lazy {
    scope.initializer()
}
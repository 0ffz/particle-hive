package me.dvyy.particles.compute.helpers.types

import de.fabmax.kool.modules.ksl.lang.KslExprInt1
import de.fabmax.kool.modules.ksl.lang.KslExprInt3
import de.fabmax.kool.modules.ksl.lang.KslScopeBuilder
import de.fabmax.kool.modules.ksl.lang.fori
import me.dvyy.particles.compute.simulation.FieldsShaderProgram

class KslNeighbourCell(
    val grid: KslExprInt3,
    val cellId: KslExprInt1,
    val startIndex: KslExprInt1,
    val endIndexInclusive: KslExprInt1,
) {
    context(program: FieldsShaderProgram, scope: KslScopeBuilder)
    fun forEachParticle(block: KslScopeBuilder.(KslParticle) -> Unit) = with(scope) {
        fori(startIndex, endIndexInclusive) { i ->
            block(particle(i, "other"))
        }
    }
}
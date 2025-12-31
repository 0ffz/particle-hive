package me.dvyy.particles.compute.helpers

import de.fabmax.kool.modules.ksl.lang.*
import me.dvyy.particles.compute.helpers.types.KslNeighbourCell
import me.dvyy.particles.compute.simulation.FieldsShaderProgram

/** Loops over a third z value if config is set to threeDimensions */
internal fun KslScopeBuilder.forZIf3d(
    isThreeDimensions: Boolean,
    block: KslScopeBuilder.(KslInt) -> Unit,
) {
    if (isThreeDimensions) fori((-1).const, 2.const) { z ->
        block(z)
    } else block(0.const)
}

/**
 * Loops over grid cells surrounding cell [grid], ensuring within bounds of program.
 */
context(program: FieldsShaderProgram)
internal fun KslScopeBuilder.forNearbyGridCells(
    grid: KslExprInt3,
    block: KslScopeBuilder.(neighbour: KslNeighbourCell) -> Unit,
) {
    val isThreeDimensions = program.is3d
    fori((-1).const, 2.const) { x ->
        fori((-1).const, 2.const) { y ->
            forZIf3d(isThreeDimensions) { z -> // A third loop if set to 3-dimensions
                val cell = int3Var(grid + int3Value(x, y, z))

                // Ensure in bounds of grid
                `if`(
                    any(cell lt 0.const3).or(
                        if (isThreeDimensions) any(cell ge program.gridCells)
                        else any(cell.xy ge program.gridCells.xy)
                    )
                ) {
                    `continue`()
                }

                // Gather info about this cell
                val localCellId = int1Var(cellId(cell, program.gridCells))
                val startIndex = int1Var(program.cellOffsets[localCellId])
                val endIndex = int1Var(program.cellOffsetsEnd[localCellId])

                block(
                    KslNeighbourCell(
                        grid = cell,
                        cellId = localCellId,
                        startIndex = startIndex,
                        endIndexInclusive = endIndex,
                    )
                )
            }
        }
    }
}

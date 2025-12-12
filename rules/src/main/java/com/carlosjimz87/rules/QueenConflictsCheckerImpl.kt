package com.carlosjimz87.rules

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.Conflicts

class QueenConflictsCheckerImpl : QueenConflictsChecker {

    override fun conflicts(size: Int, queens: Set<Cell>): Conflicts {
        if (queens.isEmpty()) return Conflicts.Empty

        // Group queens by attack lines
        val byRow = queens.groupBy { it.row }
        val byCol = queens.groupBy { it.col }
        val byDiag = queens.groupBy { it.row - it.col } // main diagonal
        val byAnti = queens.groupBy { it.row + it.col } // anti diagonal

        // Collect all "lines" that actually have conflicts
        val conflictLines = buildList<List<Cell>> {
            addAll(byRow.values.filter { it.size > 1 })
            addAll(byCol.values.filter { it.size > 1 })
            addAll(byDiag.values.filter { it.size > 1 })
            addAll(byAnti.values.filter { it.size > 1 })
        }

        if (conflictLines.isEmpty()) return Conflicts.Empty

        // Build adjacency map: each queen -> set of queens it conflicts with
        val map = mutableMapOf<Cell, MutableSet<Cell>>()

        fun addEdges(line: List<Cell>) {
            for (a in line) {
                val set = map.getOrPut(a) { mutableSetOf() }
                for (b in line) {
                    if (a != b) set.add(b)
                }
            }
        }

        conflictLines.forEach(::addEdges)

        return Conflicts(map.mapValues { it.value.toSet() })
    }
}
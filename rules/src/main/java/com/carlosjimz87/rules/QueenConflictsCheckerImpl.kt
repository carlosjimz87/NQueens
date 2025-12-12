package com.carlosjimz87.rules

import com.carlosjimz87.rules.model.Cell
import com.carlosjimz87.rules.model.ConflictPair
import com.carlosjimz87.rules.model.Conflicts
class QueenConflictsCheckerImpl : QueenConflictsChecker {

    override fun check(size: Int, queens: Set<Cell>): Conflicts {
        if (queens.isEmpty()) return Conflicts.Empty

        val byRow = queens.groupBy { it.row }
        val byCol = queens.groupBy { it.col }
        val byDiag = queens.groupBy { it.row - it.col }
        val byAnti = queens.groupBy { it.row + it.col }

        val conflictLines = buildList {
            addAll(byRow.values.filter { it.size > 1 })
            addAll(byCol.values.filter { it.size > 1 })
            addAll(byDiag.values.filter { it.size > 1 })
            addAll(byAnti.values.filter { it.size > 1 })
        }

        if (conflictLines.isEmpty()) return Conflicts.Empty

        val map = mutableMapOf<Cell, MutableSet<Cell>>()
        val uniquePairs = LinkedHashSet<Long>()
        val pairs = ArrayList<ConflictPair>()

        fun pairKey(a: Cell, b: Cell): Long {
            val ha = a.hashCode().toLong()
            val hb = b.hashCode().toLong()
            val min = minOf(ha, hb)
            val max = maxOf(ha, hb)
            return (min shl 32) xor (max and 0xffffffffL)
        }

        fun addEdges(line: List<Cell>) {
            for (i in line.indices) {
                val a = line[i]
                val setA = map.getOrPut(a) { mutableSetOf() }

                for (j in line.indices) {
                    if (i == j) continue
                    val b = line[j]
                    setA.add(b)

                    // unique pair list for drawing / counting lines
                    val key = pairKey(a, b)
                    if (uniquePairs.add(key)) {
                        pairs.add(ConflictPair(a = a, b = b))
                    }
                }
            }
        }

        conflictLines.forEach(::addEdges)

        return Conflicts(
            conflictsByCell = map.mapValues { it.value.toSet() },
            pairs = pairs
        )
    }
}
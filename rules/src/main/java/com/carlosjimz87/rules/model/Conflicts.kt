package com.carlosjimz87.rules.model

data class Conflicts(
    val conflictsByCell: Map<Cell, Set<Cell>>,
    val pairs: List<ConflictPair> = emptyList()
) {
    companion object {
        val Empty = Conflicts(emptyMap(), emptyList())
    }

    val hasConflicts: Boolean get() = pairs.isNotEmpty()
    val conflictLinesCount: Int get() = pairs.size
    val conflictCells: Set<Cell> get() = conflictsByCell.keys
}
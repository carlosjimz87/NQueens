package com.carlosjimz87.rules

import com.carlosjimz87.rules.model.Cell
import org.junit.Assert.*
import org.junit.Test

class QueenConflictsCheckerTest {

    private val checker = QueenConflictsCheckerImpl()

    @Test
    fun `no conflicts returns empty report`() {
        val queens = setOf(
            Cell(0, 1),
            Cell(1, 3),
            Cell(2, 0),
            Cell(3, 2)
        )
        val report = checker.conflicts(size = 4, queens = queens)
        assertFalse(report.hasConflicts)
        assertTrue(report.conflictsByCell.isEmpty())
    }

    @Test
    fun `same row conflicts`() {
        val a = Cell(0, 0)
        val b = Cell(0, 3)

        val report = checker.conflicts(4, setOf(a, b))
        assertTrue(report.hasConflicts)
        assertTrue(a in report.conflictCells)
        assertTrue(b in report.conflictCells)
        assertEquals(setOf(b), report.conflictsFor(a))
        assertEquals(setOf(a), report.conflictsFor(b))
    }

    @Test
    fun `same diagonal conflicts`() {
        val a = Cell(0, 0)
        val b = Cell(2, 2)

        val report = checker.conflicts(4, setOf(a, b))
        assertTrue(report.hasConflicts)
        assertEquals(setOf(b), report.conflictsFor(a))
    }

    @Test
    fun `anti diagonal conflicts`() {
        val a = Cell(0, 3)
        val b = Cell(1, 2)

        val report = checker.conflicts(4, setOf(a, b))
        assertTrue(report.hasConflicts)
        assertEquals(setOf(b), report.conflictsFor(a))
    }
}
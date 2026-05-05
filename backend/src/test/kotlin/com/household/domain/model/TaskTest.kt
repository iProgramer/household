package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class TaskTest {

    @Test
    fun `create task with valid title`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)

        assertEquals("Staubsaugen", task.title)
        assertEquals(TaskStatus.OPEN, task.status)
        assertNull(task.date)
        assertEquals(HOUSEHOLD_ID, task.householdId)
    }

    @Test
    fun `create task trims whitespace from title`() {
        val task = Task.create(HOUSEHOLD_ID, "  Staubsaugen  ", null)

        assertEquals("Staubsaugen", task.title)
    }

    @Test
    fun `create task with date`() {
        val date = LocalDate.of(2026, 5, 5)
        val task = Task.create(HOUSEHOLD_ID, "Müll rausbringen", date)

        assertEquals(date, task.date)
    }

    @Test
    fun `create task with blank title throws`() {
        assertThrows<IllegalArgumentException> {
            Task.create(HOUSEHOLD_ID, "   ", null)
        }
    }

    @Test
    fun `create task with empty title throws`() {
        assertThrows<IllegalArgumentException> {
            Task.create(HOUSEHOLD_ID, "", null)
        }
    }

    @Test
    fun `complete task changes status to DONE`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)

        val completed = task.complete()

        assertEquals(TaskStatus.DONE, completed.status)
    }

    @Test
    fun `complete returns new instance, original unchanged`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)

        val completed = task.complete()

        assertNotSame(task, completed)
        assertEquals(TaskStatus.OPEN, task.status)
    }

    @Test
    fun `each created task has a unique id`() {
        val a = Task.create(HOUSEHOLD_ID, "Aufgabe A", null)
        val b = Task.create(HOUSEHOLD_ID, "Aufgabe B", null)

        assert(a.id != b.id)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
        assertNull(task.assignedTo)
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
    fun `create task with assignment`() {
        val memberId = MemberId(UUID.randomUUID())
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null, assignedTo = memberId)

        assertEquals(memberId, task.assignedTo)
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

    @Test
    fun `reschedule sets new date`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)
        val newDate = LocalDate.of(2026, 5, 10)

        val rescheduled = task.reschedule(newDate)

        assertEquals(newDate, rescheduled.date)
    }

    @Test
    fun `reschedule to null clears date`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", LocalDate.of(2026, 5, 5))

        val rescheduled = task.reschedule(null)

        assertNull(rescheduled.date)
    }

    @Test
    fun `reschedule returns new instance, original unchanged`() {
        val original = Task.create(HOUSEHOLD_ID, "Staubsaugen", LocalDate.of(2026, 5, 5))
        val newDate = LocalDate.of(2026, 5, 10)

        val rescheduled = original.reschedule(newDate)

        assertNotSame(original, rescheduled)
        assertEquals(LocalDate.of(2026, 5, 5), original.date)
    }

    @Test
    fun `reassign sets new member`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)
        val memberId = MemberId(UUID.randomUUID())

        val reassigned = task.reassign(memberId)

        assertEquals(memberId, reassigned.assignedTo)
    }

    @Test
    fun `reassign to null clears assignment`() {
        val memberId = MemberId(UUID.randomUUID())
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null, assignedTo = memberId)

        val reassigned = task.reassign(null)

        assertNull(reassigned.assignedTo)
    }

    @Test
    fun `nextOccurrence returns null when no recurrence rule`() {
        val task = Task.create(HOUSEHOLD_ID, "Einmalig", LocalDate.of(2026, 5, 9))

        assertNull(task.nextOccurrence())
    }

    @Test
    fun `nextOccurrence returns null when no date`() {
        val task = Task.create(HOUSEHOLD_ID, "Ungeplant", null, recurrenceRule = RecurrenceRule.Weekly)

        assertNull(task.nextOccurrence())
    }

    @Test
    fun `nextOccurrence returns new task with next date`() {
        val date = LocalDate.of(2026, 5, 9) // Saturday
        val task = Task.create(HOUSEHOLD_ID, "Bad putzen", date, recurrenceRule = RecurrenceRule.Weekly)

        val next = task.nextOccurrence()

        assertNotNull(next)
        assertEquals(LocalDate.of(2026, 5, 16), next!!.date)
        assertEquals("Bad putzen", next.title)
        assertEquals(TaskStatus.OPEN, next.status)
        assertEquals(RecurrenceRule.Weekly, next.recurrenceRule)
    }

    @Test
    fun `nextOccurrence assigns new unique id`() {
        val date = LocalDate.of(2026, 5, 9)
        val task = Task.create(HOUSEHOLD_ID, "Bad putzen", date, recurrenceRule = RecurrenceRule.Daily)

        val next = task.nextOccurrence()!!

        assert(next.id != task.id)
    }

    @Test
    fun `nextOccurrence preserves assignedTo`() {
        val memberId = MemberId(UUID.randomUUID())
        val date = LocalDate.of(2026, 5, 9)
        val task = Task.create(HOUSEHOLD_ID, "Einkaufen", date, assignedTo = memberId, recurrenceRule = RecurrenceRule.Weekly)

        val next = task.nextOccurrence()!!

        assertEquals(memberId, next.assignedTo)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

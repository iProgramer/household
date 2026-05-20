package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.RecurrenceRule
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskNotFoundException
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CreateTaskCommand
import com.household.domain.port.`in`.UpdateTaskCommand
import com.household.domain.port.out.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class TaskServiceTest {

    private val taskRepository = mockk<TaskRepository>()
    private val service = TaskService(taskRepository)

    @Test
    fun `create saves task and returns it`() {
        val command = CreateTaskCommand(HOUSEHOLD_ID, "Staubsaugen", null)
        val slot = slot<Task>()
        every { taskRepository.save(capture(slot)) } answers { slot.captured }

        val result = service.create(command)

        assertEquals("Staubsaugen", result.title)
        assertEquals(TaskStatus.OPEN, result.status)
        assertEquals(HOUSEHOLD_ID, result.householdId)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `create passes date and assignment from command`() {
        val date = LocalDate.of(2026, 5, 5)
        val memberId = MemberId(UUID.randomUUID())
        val command = CreateTaskCommand(HOUSEHOLD_ID, "Termin", date, assignedTo = memberId)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals(date, result.date)
        assertEquals(memberId, result.assignedTo)
    }

    @Test
    fun `complete finds task, marks done, saves and returns it`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)
        every { taskRepository.findById(task.id) } returns task
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.complete(task.id)

        assertEquals(TaskStatus.DONE, result.status)
        verify { taskRepository.save(match { it.status == TaskStatus.DONE }) }
    }

    @Test
    fun `complete throws TaskNotFoundException when task does not exist`() {
        val unknownId = TaskId(UUID.randomUUID())
        every { taskRepository.findById(unknownId) } returns null

        assertThrows<TaskNotFoundException> { service.complete(unknownId) }
    }

    @Test
    fun `getTodayTasks delegates to repository with today's date`() {
        val today = LocalDate.now()
        every { taskRepository.findAllByHouseholdIdAndDate(HOUSEHOLD_ID, today) } returns emptyList()

        val result = service.getTodayTasks(HOUSEHOLD_ID)

        assertTrue(result.isEmpty())
        verify { taskRepository.findAllByHouseholdIdAndDate(HOUSEHOLD_ID, today) }
    }

    @Test
    fun `getTodayTasks returns tasks from repository`() {
        val today = LocalDate.now()
        val tasks = listOf(
            Task.create(HOUSEHOLD_ID, "Aufgabe A", today),
            Task.create(HOUSEHOLD_ID, "Aufgabe B", today),
        )
        every { taskRepository.findAllByHouseholdIdAndDate(HOUSEHOLD_ID, today) } returns tasks

        val result = service.getTodayTasks(HOUSEHOLD_ID)

        assertEquals(2, result.size)
    }

    @Test
    fun `getWeekTasks delegates to repository with correct date range`() {
        val start = LocalDate.of(2026, 5, 4)
        val end = start.plusDays(6)
        every { taskRepository.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, end) } returns emptyList()

        service.getWeekTasks(HOUSEHOLD_ID, start)

        verify { taskRepository.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, end) }
    }

    @Test
    fun `getWeekTasks returns tasks from repository`() {
        val start = LocalDate.of(2026, 5, 4)
        val tasks = listOf(
            Task.create(HOUSEHOLD_ID, "Montag", start),
            Task.create(HOUSEHOLD_ID, "Mittwoch", start.plusDays(2)),
        )
        every { taskRepository.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, start.plusDays(6)) } returns tasks

        val result = service.getWeekTasks(HOUSEHOLD_ID, start)

        assertEquals(2, result.size)
    }

    @Test
    fun `getUnplannedTasks delegates to repository`() {
        every { taskRepository.findAllOpenByHouseholdIdAndDateIsNull(HOUSEHOLD_ID) } returns emptyList()

        service.getUnplannedTasks(HOUSEHOLD_ID)

        verify { taskRepository.findAllOpenByHouseholdIdAndDateIsNull(HOUSEHOLD_ID) }
    }

    @Test
    fun `getUnplannedTasks returns open tasks without date`() {
        val tasks = listOf(Task.create(HOUSEHOLD_ID, "Irgendwann", null))
        every { taskRepository.findAllOpenByHouseholdIdAndDateIsNull(HOUSEHOLD_ID) } returns tasks

        val result = service.getUnplannedTasks(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Irgendwann", result[0].title)
    }

    @Test
    fun `update reschedules and reassigns task`() {
        val memberId = MemberId(UUID.randomUUID())
        val task = Task.create(HOUSEHOLD_ID, "Aufgabe", null)
        val newDate = LocalDate.of(2026, 5, 10)
        val command = UpdateTaskCommand(task.id, newDate, memberId)
        every { taskRepository.findById(task.id) } returns task
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.update(command)

        assertEquals(newDate, result.date)
        assertEquals(memberId, result.assignedTo)
        verify { taskRepository.save(match { it.date == newDate && it.assignedTo == memberId }) }
    }

    @Test
    fun `update throws TaskNotFoundException when task does not exist`() {
        val unknownId = TaskId(UUID.randomUUID())
        every { taskRepository.findById(unknownId) } returns null

        assertThrows<TaskNotFoundException> {
            service.update(UpdateTaskCommand(unknownId, null, null))
        }
    }

    @Test
    fun `complete non-recurring task does not create next occurrence`() {
        val task = Task.create(HOUSEHOLD_ID, "Einmalig", LocalDate.now())
        every { taskRepository.findById(task.id) } returns task
        every { taskRepository.save(any()) } answers { firstArg() }

        service.complete(task.id)

        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `complete recurring task creates next occurrence`() {
        val date = LocalDate.of(2026, 5, 9)
        val task = Task.create(HOUSEHOLD_ID, "Bad putzen", date, recurrenceRule = RecurrenceRule.Weekly)
        every { taskRepository.findById(task.id) } returns task
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.complete(task.id)

        assertEquals(TaskStatus.DONE, result.status)
        verify(exactly = 2) { taskRepository.save(any()) }
        verify { taskRepository.save(match { it.status == TaskStatus.OPEN && it.date == LocalDate.of(2026, 5, 16) }) }
    }

    @Test
    fun `create passes recurrence rule from command`() {
        val command = CreateTaskCommand(HOUSEHOLD_ID, "Bad putzen", LocalDate.now(), recurrenceRule = RecurrenceRule.Weekly)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals(RecurrenceRule.Weekly, result.recurrenceRule)
    }

    @Test
    fun `getOverdueTasks delegates to repository with today as boundary`() {
        val today = LocalDate.now()
        every { taskRepository.findAllOpenByHouseholdIdAndDateBefore(HOUSEHOLD_ID, today) } returns emptyList()

        service.getOverdueTasks(HOUSEHOLD_ID)

        verify { taskRepository.findAllOpenByHouseholdIdAndDateBefore(HOUSEHOLD_ID, today) }
    }

    @Test
    fun `getOverdueTasks returns only open tasks with past date`() {
        val yesterday = LocalDate.now().minusDays(1)
        val tasks = listOf(Task.create(HOUSEHOLD_ID, "Vergessen", yesterday))
        every { taskRepository.findAllOpenByHouseholdIdAndDateBefore(HOUSEHOLD_ID, LocalDate.now()) } returns tasks

        val result = service.getOverdueTasks(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Vergessen", result[0].title)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

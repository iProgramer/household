package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskNotFoundException
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CreateTaskCommand
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
    fun `create passes date from command to task`() {
        val date = LocalDate.of(2026, 5, 5)
        val command = CreateTaskCommand(HOUSEHOLD_ID, "Termin", date)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals(date, result.date)
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

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

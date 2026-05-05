package com.household.adapter.`in`.web

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskNotFoundException
import com.household.domain.port.`in`.CompleteTaskUseCase
import com.household.domain.port.`in`.CreateTaskUseCase
import com.household.domain.port.`in`.GetTodayTasksUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(TaskController::class)
class TaskControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var createTask: CreateTaskUseCase

    @MockkBean
    lateinit var getTodayTasks: GetTodayTasksUseCase

    @MockkBean
    lateinit var completeTask: CompleteTaskUseCase

    @Test
    fun `POST creates task and returns 201 with task body`() {
        val task = Task.create(DEMO_HOUSEHOLD_ID, "Staubsaugen", LocalDate.of(2026, 5, 5))
        every { createTask.create(any()) } returns task

        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Staubsaugen", "date": "2026-05-05"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Staubsaugen") }
            jsonPath("$.status") { value("OPEN") }
            jsonPath("$.date") { value("2026-05-05") }
            jsonPath("$.id") { exists() }
        }
    }

    @Test
    fun `POST with blank title returns 400`() {
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST without title returns 400`() {
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `GET today returns list of tasks`() {
        val tasks = listOf(
            Task.create(DEMO_HOUSEHOLD_ID, "Müll rausbringen", LocalDate.now()),
            Task.create(DEMO_HOUSEHOLD_ID, "Bad putzen", LocalDate.now()),
        )
        every { getTodayTasks.getTodayTasks(DEMO_HOUSEHOLD_ID) } returns tasks

        mockMvc.get("/api/tasks/today").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Müll rausbringen") }
            jsonPath("$[1].title") { value("Bad putzen") }
        }
    }

    @Test
    fun `GET today returns empty list when no tasks`() {
        every { getTodayTasks.getTodayTasks(DEMO_HOUSEHOLD_ID) } returns emptyList()

        mockMvc.get("/api/tasks/today").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST complete returns completed task`() {
        val task = Task.create(DEMO_HOUSEHOLD_ID, "Staubsaugen", null)
        val completed = task.complete()
        every { completeTask.complete(task.id) } returns completed

        mockMvc.post("/api/tasks/${task.id.value}/complete").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("DONE") }
        }
    }

    @Test
    fun `POST complete returns 404 when task not found`() {
        val unknownId = TaskId(UUID.randomUUID())
        every { completeTask.complete(unknownId) } throws TaskNotFoundException(unknownId)

        mockMvc.post("/api/tasks/${unknownId.value}/complete").andExpect {
            status { isNotFound() }
            jsonPath("$.error") { exists() }
        }
    }

    companion object {
        val DEMO_HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

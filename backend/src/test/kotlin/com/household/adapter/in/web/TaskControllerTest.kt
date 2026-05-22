package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskNotFoundException
import com.household.domain.port.`in`.CompleteTaskUseCase
import com.household.domain.port.`in`.CreateTaskUseCase
import com.household.domain.port.`in`.GetOverdueTasksUseCase
import com.household.domain.port.`in`.GetTodayTasksUseCase
import com.household.domain.port.`in`.GetUnplannedTasksUseCase
import com.household.domain.port.`in`.GetWeekTasksUseCase
import com.household.domain.port.`in`.ReopenTaskUseCase
import com.household.domain.port.`in`.UpdateTaskUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(TaskController::class)
@Import(SecurityConfig::class)
class TaskControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var createTask: CreateTaskUseCase

    @MockkBean
    lateinit var getTodayTasks: GetTodayTasksUseCase

    @MockkBean
    lateinit var getWeekTasks: GetWeekTasksUseCase

    @MockkBean
    lateinit var getUnplannedTasks: GetUnplannedTasksUseCase

    @MockkBean
    lateinit var getOverdueTasks: GetOverdueTasksUseCase

    @MockkBean
    lateinit var completeTask: CompleteTaskUseCase

    @MockkBean
    lateinit var reopenTask: ReopenTaskUseCase

    @MockkBean
    lateinit var updateTask: UpdateTaskUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `POST creates task and returns 201`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", LocalDate.of(2026, 5, 5))
        every { createTask.create(any()) } returns task

        mockMvc.post("/api/tasks") {
            header("Authorization", "Bearer valid-token")
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
    fun `POST creates task with assignment`() {
        val assigneeId = UUID.randomUUID()
        val task = Task.create(HOUSEHOLD_ID, "Einkaufen", null, MemberId(assigneeId))
        every { createTask.create(any()) } returns task

        mockMvc.post("/api/tasks") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Einkaufen", "assignedTo": "$assigneeId"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.assignedTo") { value(assigneeId.toString()) }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Staubsaugen"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST with blank title returns 400`() {
        mockMvc.post("/api/tasks") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `GET today returns list of tasks`() {
        val tasks = listOf(
            Task.create(HOUSEHOLD_ID, "Müll rausbringen", LocalDate.now()),
            Task.create(HOUSEHOLD_ID, "Bad putzen", LocalDate.now()),
        )
        every { getTodayTasks.getTodayTasks(HOUSEHOLD_ID) } returns tasks

        mockMvc.get("/api/tasks/today") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Müll rausbringen") }
        }
    }

    @Test
    fun `GET today without token returns 401`() {
        mockMvc.get("/api/tasks/today").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST complete returns completed task`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null)
        val completed = task.complete()
        every { completeTask.complete(task.id) } returns completed

        mockMvc.post("/api/tasks/${task.id.value}/complete") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("DONE") }
        }
    }

    @Test
    fun `POST complete returns 404 when task not found`() {
        val unknownId = TaskId(UUID.randomUUID())
        every { completeTask.complete(unknownId) } throws TaskNotFoundException(unknownId)

        mockMvc.post("/api/tasks/${unknownId.value}/complete") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `GET week returns tasks for given start date`() {
        val start = LocalDate.of(2026, 5, 4)
        val tasks = listOf(
            Task.create(HOUSEHOLD_ID, "Montag-Aufgabe", start),
            Task.create(HOUSEHOLD_ID, "Freitag-Aufgabe", start.plusDays(4)),
        )
        every { getWeekTasks.getWeekTasks(HOUSEHOLD_ID, start) } returns tasks

        mockMvc.get("/api/tasks/week?startDate=2026-05-04") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Montag-Aufgabe") }
        }
    }

    @Test
    fun `GET week without token returns 401`() {
        mockMvc.get("/api/tasks/week?startDate=2026-05-04").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET unplanned returns tasks without date`() {
        val tasks = listOf(Task.create(HOUSEHOLD_ID, "Irgendwann", null))
        every { getUnplannedTasks.getUnplannedTasks(HOUSEHOLD_ID) } returns tasks

        mockMvc.get("/api/tasks/unplanned") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].title") { value("Irgendwann") }
        }
    }

    @Test
    fun `GET unplanned without token returns 401`() {
        mockMvc.get("/api/tasks/unplanned").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `PATCH updates task and returns updated response`() {
        val task = Task.create(HOUSEHOLD_ID, "Aufgabe", null)
        val newDate = LocalDate.of(2026, 5, 10)
        val updated = task.reschedule(newDate)
        every { updateTask.update(any()) } returns updated

        mockMvc.patch("/api/tasks/${task.id.value}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-10"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.date") { value("2026-05-10") }
            jsonPath("$.title") { value("Aufgabe") }
        }
    }

    @Test
    fun `PATCH with title renames task`() {
        val task = Task.create(HOUSEHOLD_ID, "Alt", null)
        val renamed = task.rename("Neu")
        every { updateTask.update(any()) } returns renamed

        mockMvc.patch("/api/tasks/${task.id.value}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": null, "title": "Neu"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Neu") }
        }
    }

    @Test
    fun `PATCH returns 404 when task not found`() {
        val unknownId = TaskId(UUID.randomUUID())
        every { updateTask.update(any()) } throws TaskNotFoundException(unknownId)

        mockMvc.patch("/api/tasks/${unknownId.value}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-10"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PATCH without token returns 401`() {
        mockMvc.patch("/api/tasks/${UUID.randomUUID()}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": null}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST creates recurring task and returns recurrence in response`() {
        val task = Task.create(HOUSEHOLD_ID, "Bad putzen", LocalDate.of(2026, 5, 9), recurrenceRule = com.household.domain.model.RecurrenceRule.Weekly)
        every { createTask.create(any()) } returns task

        mockMvc.post("/api/tasks") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Bad putzen", "date": "2026-05-09", "recurrence": {"type": "WEEKLY"}}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.recurrence.type") { value("WEEKLY") }
        }
    }

    @Test
    fun `POST with unknown recurrence type returns 400`() {
        every { createTask.create(any()) } throws IllegalArgumentException("Unknown recurrence type: YEARLY")

        mockMvc.post("/api/tasks") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Aufgabe", "recurrence": {"type": "YEARLY"}}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

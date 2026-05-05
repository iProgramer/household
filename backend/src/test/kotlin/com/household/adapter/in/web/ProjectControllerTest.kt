package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.Project
import com.household.domain.model.ProjectDetail
import com.household.domain.model.ProjectId
import com.household.domain.model.ProjectNotFoundException
import com.household.domain.model.ProjectWithProgress
import com.household.domain.port.`in`.CompleteProjectUseCase
import com.household.domain.port.`in`.CreateProjectUseCase
import com.household.domain.port.`in`.GetProjectUseCase
import com.household.domain.port.`in`.GetProjectsUseCase
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
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(ProjectController::class)
@Import(SecurityConfig::class)
class ProjectControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var createProject: CreateProjectUseCase

    @MockkBean
    lateinit var getProjects: GetProjectsUseCase

    @MockkBean
    lateinit var getProject: GetProjectUseCase

    @MockkBean
    lateinit var completeProject: CompleteProjectUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `POST creates project and returns 201`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller aufräumen", "Alles in Kisten sortiert")
        every { createProject.create(any()) } returns project

        mockMvc.post("/api/projects") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Keller aufräumen", "goal": "Alles in Kisten sortiert"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Keller aufräumen") }
            jsonPath("$.goal") { value("Alles in Kisten sortiert") }
            jsonPath("$.status") { value("ACTIVE") }
        }
    }

    @Test
    fun `POST with blank title returns 400`() {
        mockMvc.post("/api/projects") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "", "goal": "Ziel"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/projects") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Keller", "goal": "Ziel"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET list returns projects with progress`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        val withProgress = ProjectWithProgress(project, totalSteps = 3, completedSteps = 1)
        every { getProjects.getProjects(HOUSEHOLD_ID) } returns listOf(withProgress)

        mockMvc.get("/api/projects") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].title") { value("Keller") }
            jsonPath("$[0].totalSteps") { value(3) }
            jsonPath("$[0].completedSteps") { value(1) }
        }
    }

    @Test
    fun `GET detail returns project with tasks`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        val detail = ProjectDetail(project, emptyList())
        every { getProject.getProject(project.id) } returns detail

        mockMvc.get("/api/projects/${project.id.value}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Keller") }
            jsonPath("$.tasks") { isArray() }
        }
    }

    @Test
    fun `GET detail returns 404 when not found`() {
        val unknownId = ProjectId(UUID.randomUUID())
        every { getProject.getProject(unknownId) } throws ProjectNotFoundException(unknownId)

        mockMvc.get("/api/projects/${unknownId.value}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `POST complete returns completed project`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig").complete()
        every { completeProject.complete(project.id) } returns project

        mockMvc.post("/api/projects/${project.id.value}/complete") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("DONE") }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project
import com.household.domain.model.ProjectId
import com.household.domain.model.ProjectNotFoundException
import com.household.domain.model.ProjectStatus
import com.household.domain.model.Task
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CreateProjectCommand
import com.household.domain.port.out.ProjectRepository
import com.household.domain.port.out.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class ProjectServiceTest {

    private val projectRepository = mockk<ProjectRepository>()
    private val taskRepository = mockk<TaskRepository>()
    private val service = ProjectService(projectRepository, taskRepository)

    @Test
    fun `create saves project and returns it`() {
        val command = CreateProjectCommand(HOUSEHOLD_ID, "Keller aufräumen", "Alles sortiert")
        every { projectRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Keller aufräumen", result.title)
        assertEquals(ProjectStatus.ACTIVE, result.status)
        verify(exactly = 1) { projectRepository.save(any()) }
    }

    @Test
    fun `getProjects returns projects with progress`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        val tasks = listOf(
            Task.create(HOUSEHOLD_ID, "Schritt 1", LocalDate.now(), projectId = project.id),
            Task.create(HOUSEHOLD_ID, "Schritt 2", null, projectId = project.id).complete(),
        )
        every { projectRepository.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(project)
        every { taskRepository.findAllByProjectId(project.id) } returns tasks

        val result = service.getProjects(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals(2, result[0].totalSteps)
        assertEquals(1, result[0].completedSteps)
    }

    @Test
    fun `getProjects with no tasks shows zero progress`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        every { projectRepository.findAllByHouseholdId(HOUSEHOLD_ID) } returns listOf(project)
        every { taskRepository.findAllByProjectId(project.id) } returns emptyList()

        val result = service.getProjects(HOUSEHOLD_ID)

        assertEquals(0, result[0].totalSteps)
        assertEquals(0, result[0].completedSteps)
    }

    @Test
    fun `getProject returns detail with tasks`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        val tasks = listOf(Task.create(HOUSEHOLD_ID, "Schritt 1", null, projectId = project.id))
        every { projectRepository.findById(project.id) } returns project
        every { taskRepository.findAllByProjectId(project.id) } returns tasks

        val result = service.getProject(project.id)

        assertEquals(project.title, result.project.title)
        assertEquals(1, result.tasks.size)
    }

    @Test
    fun `getProject throws when not found`() {
        val unknownId = ProjectId(UUID.randomUUID())
        every { projectRepository.findById(unknownId) } returns null

        assertThrows<ProjectNotFoundException> { service.getProject(unknownId) }
    }

    @Test
    fun `complete marks project as DONE`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")
        every { projectRepository.findById(project.id) } returns project
        every { projectRepository.save(any()) } answers { firstArg() }

        val result = service.complete(project.id)

        assertEquals(ProjectStatus.DONE, result.status)
        verify { projectRepository.save(match { it.status == ProjectStatus.DONE }) }
    }

    @Test
    fun `complete throws when project not found`() {
        val unknownId = ProjectId(UUID.randomUUID())
        every { projectRepository.findById(unknownId) } returns null

        assertThrows<ProjectNotFoundException> { service.complete(unknownId) }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project
import com.household.domain.model.ProjectDetail
import com.household.domain.model.ProjectId
import com.household.domain.model.ProjectNotFoundException
import com.household.domain.model.ProjectWithProgress
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CompleteProjectUseCase
import com.household.domain.port.`in`.CreateProjectCommand
import com.household.domain.port.`in`.CreateProjectUseCase
import com.household.domain.port.`in`.GetProjectUseCase
import com.household.domain.port.`in`.GetProjectsUseCase
import com.household.domain.port.out.ProjectRepository
import com.household.domain.port.out.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) : CreateProjectUseCase, GetProjectsUseCase, GetProjectUseCase, CompleteProjectUseCase {

    override fun create(command: CreateProjectCommand): Project =
        projectRepository.save(Project.create(command.householdId, command.title, command.goal))

    @Transactional(readOnly = true)
    override fun getProjects(householdId: HouseholdId): List<ProjectWithProgress> =
        projectRepository.findAllByHouseholdId(householdId).map { project ->
            val tasks = taskRepository.findAllByProjectId(project.id)
            ProjectWithProgress(
                project = project,
                totalSteps = tasks.size,
                completedSteps = tasks.count { it.status == TaskStatus.DONE },
            )
        }

    @Transactional(readOnly = true)
    override fun getProject(projectId: ProjectId): ProjectDetail {
        val project = projectRepository.findById(projectId) ?: throw ProjectNotFoundException(projectId)
        val tasks = taskRepository.findAllByProjectId(projectId)
        return ProjectDetail(project, tasks)
    }

    override fun complete(projectId: ProjectId): Project {
        val project = projectRepository.findById(projectId) ?: throw ProjectNotFoundException(projectId)
        return projectRepository.save(project.complete())
    }
}

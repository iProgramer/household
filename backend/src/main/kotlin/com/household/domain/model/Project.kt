package com.household.domain.model

import java.util.UUID

@JvmInline value class ProjectId(val value: UUID)

enum class ProjectStatus { ACTIVE, DONE }

class ProjectNotFoundException(id: ProjectId) : RuntimeException("Project not found: ${id.value}")

data class Project(
    val id: ProjectId,
    val householdId: HouseholdId,
    val title: String,
    val goal: String,
    val status: ProjectStatus,
) {
    companion object {
        fun create(householdId: HouseholdId, title: String, goal: String): Project {
            require(title.isNotBlank()) { "Title must not be blank" }
            require(goal.isNotBlank()) { "Goal must not be blank" }
            return Project(
                id = ProjectId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
                goal = goal.trim(),
                status = ProjectStatus.ACTIVE,
            )
        }
    }

    fun complete(): Project = copy(status = ProjectStatus.DONE)
}

data class ProjectWithProgress(val project: Project, val totalSteps: Int, val completedSteps: Int)

data class ProjectDetail(val project: Project, val tasks: List<Task>)

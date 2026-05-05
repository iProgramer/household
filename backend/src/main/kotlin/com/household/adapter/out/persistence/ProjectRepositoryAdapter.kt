package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project
import com.household.domain.model.ProjectId
import com.household.domain.model.ProjectStatus
import com.household.domain.port.out.ProjectRepository
import org.springframework.stereotype.Component

@Component
class ProjectRepositoryAdapter(
    private val jpa: ProjectJpaRepository,
) : ProjectRepository {

    override fun save(project: Project): Project =
        jpa.save(project.toJpaEntity()).toDomain()

    override fun findById(id: ProjectId): Project? =
        jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAllByHouseholdId(householdId: HouseholdId): List<Project> =
        jpa.findAllByHouseholdId(householdId.value).map { it.toDomain() }

    private fun Project.toJpaEntity() = ProjectJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        goal = goal,
        status = status.name,
    )

    private fun ProjectJpaEntity.toDomain() = Project(
        id = ProjectId(id),
        householdId = HouseholdId(householdId),
        title = title,
        goal = goal,
        status = ProjectStatus.valueOf(status),
    )
}

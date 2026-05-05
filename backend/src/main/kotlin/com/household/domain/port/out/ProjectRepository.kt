package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project
import com.household.domain.model.ProjectId

interface ProjectRepository {
    fun save(project: Project): Project
    fun findById(id: ProjectId): Project?
    fun findAllByHouseholdId(householdId: HouseholdId): List<Project>
}

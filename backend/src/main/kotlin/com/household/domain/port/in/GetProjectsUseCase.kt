package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.ProjectWithProgress

interface GetProjectsUseCase {
    fun getProjects(householdId: HouseholdId): List<ProjectWithProgress>
}

package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project

data class CreateProjectCommand(
    val householdId: HouseholdId,
    val title: String,
    val goal: String,
)

interface CreateProjectUseCase {
    fun create(command: CreateProjectCommand): Project
}

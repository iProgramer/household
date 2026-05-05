package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task

interface GetUnplannedTasksUseCase {
    fun getUnplannedTasks(householdId: HouseholdId): List<Task>
}

package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task

interface GetOverdueTasksUseCase {
    fun getOverdueTasks(householdId: HouseholdId): List<Task>
}

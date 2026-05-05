package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task

interface GetTodayTasksUseCase {
    fun getTodayTasks(householdId: HouseholdId): List<Task>
}

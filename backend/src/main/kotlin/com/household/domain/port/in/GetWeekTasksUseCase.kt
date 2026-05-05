package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import java.time.LocalDate

interface GetWeekTasksUseCase {
    fun getWeekTasks(householdId: HouseholdId, startDate: LocalDate): List<Task>
}

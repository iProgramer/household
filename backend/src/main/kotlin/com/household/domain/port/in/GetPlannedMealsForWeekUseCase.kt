package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import java.time.LocalDate

interface GetPlannedMealsForWeekUseCase {
    fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<PlannedMeal>
}

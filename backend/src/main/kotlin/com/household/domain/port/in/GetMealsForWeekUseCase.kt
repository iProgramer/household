package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import java.time.LocalDate

interface GetMealsForWeekUseCase {
    fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<Meal>
}

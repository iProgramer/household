package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import java.time.LocalDate

interface GetMealsForDateUseCase {
    fun getForDate(householdId: HouseholdId, date: LocalDate): List<Meal>
}

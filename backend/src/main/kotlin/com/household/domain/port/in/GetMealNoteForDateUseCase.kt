package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import java.time.LocalDate

interface GetMealNoteForDateUseCase {
    fun getForDate(householdId: HouseholdId, date: LocalDate): MealNote?
}

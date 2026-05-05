package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import java.time.LocalDate

interface MealNoteRepository {
    fun save(mealNote: MealNote): MealNote
    fun findByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): MealNote?
    fun findAllByHouseholdIdAndDateBetween(householdId: HouseholdId, start: LocalDate, end: LocalDate): List<MealNote>
}

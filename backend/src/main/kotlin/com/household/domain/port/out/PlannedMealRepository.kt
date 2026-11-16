package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import java.time.LocalDate

interface PlannedMealRepository {
    fun save(meal: PlannedMeal): PlannedMeal
    fun findById(id: PlannedMealId): PlannedMeal?
    fun findByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): PlannedMeal?
    fun findByHouseholdIdBetween(householdId: HouseholdId, from: LocalDate, to: LocalDate): List<PlannedMeal>
    fun delete(id: PlannedMealId)
}

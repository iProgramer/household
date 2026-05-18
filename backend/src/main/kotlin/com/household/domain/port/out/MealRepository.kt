package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import java.time.LocalDate

interface MealRepository {
    fun save(meal: Meal): Meal
    fun findById(id: MealId): Meal?
    fun findIdeasByHouseholdId(householdId: HouseholdId): List<Meal>
    fun findPlannedByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): List<Meal>
    fun findPlannedByHouseholdIdBetween(householdId: HouseholdId, from: LocalDate, to: LocalDate): List<Meal>
    fun delete(id: MealId)
}

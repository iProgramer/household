package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal

data class CreateMealCommand(val householdId: HouseholdId, val title: String)

interface CreateMealUseCase {
    fun create(command: CreateMealCommand): Meal
}

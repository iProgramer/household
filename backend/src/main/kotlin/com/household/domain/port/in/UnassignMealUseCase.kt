package com.household.domain.port.`in`

import com.household.domain.model.Meal
import com.household.domain.model.MealId

interface UnassignMealUseCase {
    fun unassign(id: MealId): Meal
}

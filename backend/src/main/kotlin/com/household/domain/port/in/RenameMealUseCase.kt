package com.household.domain.port.`in`

import com.household.domain.model.Meal
import com.household.domain.model.MealId

interface RenameMealUseCase {
    fun rename(id: MealId, title: String): Meal
}

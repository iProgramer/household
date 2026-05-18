package com.household.domain.port.`in`

import com.household.domain.model.MealId

interface DeleteMealUseCase {
    fun delete(id: MealId)
}

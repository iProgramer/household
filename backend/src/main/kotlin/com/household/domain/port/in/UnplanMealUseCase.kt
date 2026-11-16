package com.household.domain.port.`in`

import com.household.domain.model.PlannedMealId

interface UnplanMealUseCase {
    fun unplan(id: PlannedMealId)
}

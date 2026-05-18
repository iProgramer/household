package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal

interface GetMealIdeasUseCase {
    fun getIdeas(householdId: HouseholdId): List<Meal>
}

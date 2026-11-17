package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMealId
import java.util.UUID

data class PlannedMealFavoriteResult(val recipeId: UUID, val favorite: Boolean)

interface TogglePlannedMealFavoriteUseCase {
    fun toggleFavorite(id: PlannedMealId, householdId: HouseholdId): PlannedMealFavoriteResult
}

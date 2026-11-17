package com.household.domain.port.`in`

import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId

interface ToggleFavoriteUseCase {
    fun toggleFavorite(id: RecipeId): Recipe
}

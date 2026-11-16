package com.household.domain.port.`in`

import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId

interface RenameRecipeUseCase {
    fun rename(id: RecipeId, title: String): Recipe
}

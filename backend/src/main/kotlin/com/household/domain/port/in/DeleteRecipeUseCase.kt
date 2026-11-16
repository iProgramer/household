package com.household.domain.port.`in`

import com.household.domain.model.RecipeId

interface DeleteRecipeUseCase {
    fun delete(id: RecipeId)
}

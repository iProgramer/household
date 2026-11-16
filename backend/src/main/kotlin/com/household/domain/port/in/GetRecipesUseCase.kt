package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe

interface GetRecipesUseCase {
    fun getRecipes(householdId: HouseholdId): List<Recipe>
}

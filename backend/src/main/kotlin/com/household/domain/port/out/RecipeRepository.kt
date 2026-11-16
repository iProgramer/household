package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId

interface RecipeRepository {
    fun save(recipe: Recipe): Recipe
    fun findById(id: RecipeId): Recipe?
    fun findAllByHouseholdId(householdId: HouseholdId): List<Recipe>
    fun delete(id: RecipeId)
}

package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe

data class CreateRecipeCommand(val householdId: HouseholdId, val title: String)

interface CreateRecipeUseCase {
    fun create(command: CreateRecipeCommand): Recipe
}

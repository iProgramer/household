package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.RecipeId
import java.time.LocalDate

data class PlanMealCommand(
    val householdId: HouseholdId,
    val date: LocalDate,
    val recipeId: RecipeId?,
    val title: String?,
)

interface PlanMealUseCase {
    fun plan(command: PlanMealCommand): PlannedMeal
}

package com.household.domain.model

import java.time.LocalDate
import java.util.UUID

@JvmInline value class PlannedMealId(val value: UUID)

class PlannedMealNotFoundException(id: PlannedMealId) : RuntimeException("Planned meal not found: ${id.value}")

data class PlannedMeal(
    val id: PlannedMealId,
    val householdId: HouseholdId,
    val date: LocalDate,
    val title: String,
    val recipeId: RecipeId?,
) {
    companion object {
        fun fromRecipe(householdId: HouseholdId, date: LocalDate, recipe: Recipe): PlannedMeal =
            PlannedMeal(
                id = PlannedMealId(UUID.randomUUID()),
                householdId = householdId,
                date = date,
                title = recipe.title,
                recipeId = recipe.id,
            )

        fun adHoc(householdId: HouseholdId, date: LocalDate, title: String): PlannedMeal {
            require(title.isNotBlank()) { "Title must not be blank" }
            return PlannedMeal(
                id = PlannedMealId(UUID.randomUUID()),
                householdId = householdId,
                date = date,
                title = title.trim(),
                recipeId = null,
            )
        }
    }
}

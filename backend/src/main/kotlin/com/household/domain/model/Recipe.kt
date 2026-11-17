package com.household.domain.model

import java.util.UUID

@JvmInline value class RecipeId(val value: UUID)

class RecipeNotFoundException(id: RecipeId) : RuntimeException("Recipe not found: ${id.value}")

data class Recipe(
    val id: RecipeId,
    val householdId: HouseholdId,
    val title: String,
    val favorite: Boolean = false,
) {
    companion object {
        fun create(householdId: HouseholdId, title: String): Recipe {
            require(title.isNotBlank()) { "Title must not be blank" }
            return Recipe(
                id = RecipeId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
            )
        }
    }

    fun rename(newTitle: String): Recipe {
        require(newTitle.isNotBlank()) { "Title must not be blank" }
        return copy(title = newTitle.trim())
    }

    fun toggleFavorite(): Recipe = copy(favorite = !favorite)
}

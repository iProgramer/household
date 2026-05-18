package com.household.domain.model

import java.time.LocalDate
import java.util.UUID

@JvmInline value class MealId(val value: UUID)

class MealNotFoundException(id: MealId) : RuntimeException("Meal not found: ${id.value}")

enum class MealStatus { IDEA, PLANNED }

data class Meal(
    val id: MealId,
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate?,
    val status: MealStatus,
) {
    companion object {
        fun createIdea(householdId: HouseholdId, title: String): Meal {
            require(title.isNotBlank()) { "Title must not be blank" }
            return Meal(
                id = MealId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
                date = null,
                status = MealStatus.IDEA,
            )
        }
    }

    fun assign(date: LocalDate): Meal = copy(date = date, status = MealStatus.PLANNED)
    fun unassign(): Meal = copy(date = null, status = MealStatus.IDEA)
}

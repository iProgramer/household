package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "planned_meals")
class PlannedMealJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val date: LocalDate,
    val title: String,
    val recipeId: UUID?,
)

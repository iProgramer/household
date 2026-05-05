package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "meal_notes")
class MealNoteJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val date: LocalDate,
    val note: String,
)

package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "recipes")
class RecipeJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val title: String,
    val favorite: Boolean,
)

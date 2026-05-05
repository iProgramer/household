package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "tasks")
class TaskJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val title: String,
    val date: LocalDate?,
    val status: String,
)

package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "projects")
class ProjectJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val title: String,
    val goal: String,
    val status: String,
)

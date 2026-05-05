package com.household.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "members")
class MemberJpaEntity(
    @Id val id: UUID,
    val householdId: UUID,
    val email: String,
    val passwordHash: String,
)

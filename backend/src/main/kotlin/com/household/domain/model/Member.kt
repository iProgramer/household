package com.household.domain.model

import java.util.UUID

@JvmInline value class MemberId(val value: UUID)

data class Member(
    val id: MemberId,
    val householdId: HouseholdId,
    val email: String,
    val passwordHash: String,
) {
    fun withPasswordHash(hash: String): Member = copy(passwordHash = hash)

    companion object {
        fun create(householdId: HouseholdId, email: String, passwordHash: String): Member =
            Member(
                id = MemberId(UUID.randomUUID()),
                householdId = householdId,
                email = email.trim().lowercase(),
                passwordHash = passwordHash,
            )
    }
}

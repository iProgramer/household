package com.household.domain.model

import java.util.UUID

data class Household(
    val id: HouseholdId,
    val inviteCode: String,
) {
    companion object {
        fun create(): Household = Household(
            id = HouseholdId(UUID.randomUUID()),
            inviteCode = UUID.randomUUID().toString().replace("-", "").take(8).uppercase(),
        )
    }
}

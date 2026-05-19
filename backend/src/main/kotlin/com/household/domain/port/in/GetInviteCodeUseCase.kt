package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId

interface GetInviteCodeUseCase {
    fun getInviteCode(householdId: HouseholdId): String
}

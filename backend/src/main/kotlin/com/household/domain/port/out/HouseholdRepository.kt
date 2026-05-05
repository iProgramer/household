package com.household.domain.port.out

import com.household.domain.model.Household
import com.household.domain.model.HouseholdId

interface HouseholdRepository {
    fun save(household: Household): Household
    fun findById(id: HouseholdId): Household?
    fun findByInviteCode(inviteCode: String): Household?
}

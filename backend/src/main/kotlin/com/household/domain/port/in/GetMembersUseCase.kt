package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member

interface GetMembersUseCase {
    fun getMembers(householdId: HouseholdId): List<Member>
}

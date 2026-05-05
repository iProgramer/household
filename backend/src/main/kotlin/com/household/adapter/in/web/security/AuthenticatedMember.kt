package com.household.adapter.`in`.web.security

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId

data class AuthenticatedMember(
    val memberId: MemberId,
    val householdId: HouseholdId,
)

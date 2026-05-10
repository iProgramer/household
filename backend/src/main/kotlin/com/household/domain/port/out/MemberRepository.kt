package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member
import com.household.domain.model.MemberId

interface MemberRepository {
    fun save(member: Member): Member
    fun findById(id: MemberId): Member?
    fun findByEmail(email: String): Member?
    fun countByHouseholdId(householdId: HouseholdId): Int
    fun findByHouseholdId(householdId: HouseholdId): List<Member>
}

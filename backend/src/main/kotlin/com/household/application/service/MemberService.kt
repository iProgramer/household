package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member
import com.household.domain.port.`in`.GetMembersUseCase
import com.household.domain.port.out.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
) : GetMembersUseCase {

    override fun getMembers(householdId: HouseholdId): List<Member> =
        memberRepository.findByHouseholdId(householdId)
}

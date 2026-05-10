package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import com.household.domain.port.out.MemberRepository
import org.springframework.stereotype.Component

@Component
class MemberRepositoryAdapter(
    private val jpa: MemberJpaRepository,
) : MemberRepository {

    override fun save(member: Member): Member =
        jpa.save(member.toJpaEntity()).toDomain()

    override fun findById(id: MemberId): Member? =
        jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByEmail(email: String): Member? =
        jpa.findByEmail(email)?.toDomain()

    override fun countByHouseholdId(householdId: HouseholdId): Int =
        jpa.countByHouseholdId(householdId.value)

    override fun findByHouseholdId(householdId: HouseholdId): List<Member> =
        jpa.findAllByHouseholdId(householdId.value).map { it.toDomain() }

    private fun Member.toJpaEntity() = MemberJpaEntity(
        id = id.value,
        householdId = householdId.value,
        email = email,
        passwordHash = passwordHash,
    )

    private fun MemberJpaEntity.toDomain() = Member(
        id = MemberId(id),
        householdId = HouseholdId(householdId),
        email = email,
        passwordHash = passwordHash,
    )
}

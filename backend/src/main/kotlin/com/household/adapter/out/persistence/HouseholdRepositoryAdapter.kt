package com.household.adapter.out.persistence

import com.household.domain.model.Household
import com.household.domain.model.HouseholdId
import com.household.domain.port.out.HouseholdRepository
import org.springframework.stereotype.Component

@Component
class HouseholdRepositoryAdapter(
    private val jpa: HouseholdJpaRepository,
) : HouseholdRepository {

    override fun save(household: Household): Household =
        jpa.save(household.toJpaEntity()).toDomain()

    override fun findById(id: HouseholdId): Household? =
        jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByInviteCode(inviteCode: String): Household? =
        jpa.findByInviteCode(inviteCode)?.toDomain()

    private fun Household.toJpaEntity() = HouseholdJpaEntity(
        id = id.value,
        inviteCode = inviteCode,
    )

    private fun HouseholdJpaEntity.toDomain() = Household(
        id = HouseholdId(id),
        inviteCode = inviteCode,
    )
}

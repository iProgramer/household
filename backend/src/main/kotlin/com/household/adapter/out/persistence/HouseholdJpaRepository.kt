package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface HouseholdJpaRepository : JpaRepository<HouseholdJpaEntity, UUID> {
    fun findByInviteCode(inviteCode: String): HouseholdJpaEntity?
}

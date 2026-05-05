package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberJpaRepository : JpaRepository<MemberJpaEntity, UUID> {
    fun findByEmail(email: String): MemberJpaEntity?
    fun countByHouseholdId(householdId: UUID): Int
}

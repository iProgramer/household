package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FixedEventJpaRepository : JpaRepository<FixedEventJpaEntity, UUID> {
    fun findAllByHouseholdId(householdId: UUID): List<FixedEventJpaEntity>
}

package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectJpaRepository : JpaRepository<ProjectJpaEntity, UUID> {
    fun findAllByHouseholdId(householdId: UUID): List<ProjectJpaEntity>
}

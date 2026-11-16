package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecipeJpaRepository : JpaRepository<RecipeJpaEntity, UUID> {
    fun findAllByHouseholdId(householdId: UUID): List<RecipeJpaEntity>
}

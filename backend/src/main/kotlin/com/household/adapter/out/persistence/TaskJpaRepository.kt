package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface TaskJpaRepository : JpaRepository<TaskJpaEntity, UUID> {
    fun findAllByHouseholdIdAndDate(householdId: UUID, date: LocalDate): List<TaskJpaEntity>
}

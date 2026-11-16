package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface PlannedMealJpaRepository : JpaRepository<PlannedMealJpaEntity, UUID> {
    fun findByHouseholdIdAndDate(householdId: UUID, date: LocalDate): PlannedMealJpaEntity?
    fun findAllByHouseholdIdAndDateBetween(householdId: UUID, from: LocalDate, to: LocalDate): List<PlannedMealJpaEntity>
}

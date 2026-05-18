package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface MealJpaRepository : JpaRepository<MealJpaEntity, UUID> {
    fun findAllByHouseholdIdAndStatus(householdId: UUID, status: String): List<MealJpaEntity>
    fun findAllByHouseholdIdAndStatusAndDateBetween(
        householdId: UUID,
        status: String,
        from: LocalDate,
        to: LocalDate,
    ): List<MealJpaEntity>
    fun findAllByHouseholdIdAndStatusAndDate(
        householdId: UUID,
        status: String,
        date: LocalDate,
    ): List<MealJpaEntity>
}

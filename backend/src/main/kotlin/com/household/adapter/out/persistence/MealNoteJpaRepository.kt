package com.household.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface MealNoteJpaRepository : JpaRepository<MealNoteJpaEntity, UUID> {
    fun findByHouseholdIdAndDate(householdId: UUID, date: LocalDate): MealNoteJpaEntity?
    fun findAllByHouseholdIdAndDateBetween(householdId: UUID, start: LocalDate, end: LocalDate): List<MealNoteJpaEntity>
}

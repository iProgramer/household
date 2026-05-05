package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import com.household.domain.port.out.MealNoteRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class MealNoteRepositoryAdapter(
    private val jpa: MealNoteJpaRepository,
) : MealNoteRepository {

    override fun save(mealNote: MealNote): MealNote {
        val existingId = jpa.findByHouseholdIdAndDate(mealNote.householdId.value, mealNote.date)?.id
            ?: UUID.randomUUID()
        return jpa.save(
            MealNoteJpaEntity(existingId, mealNote.householdId.value, mealNote.date, mealNote.note)
        ).toDomain()
    }

    override fun findByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): MealNote? =
        jpa.findByHouseholdIdAndDate(householdId.value, date)?.toDomain()

    override fun findAllByHouseholdIdAndDateBetween(
        householdId: HouseholdId,
        start: LocalDate,
        end: LocalDate,
    ): List<MealNote> =
        jpa.findAllByHouseholdIdAndDateBetween(householdId.value, start, end).map { it.toDomain() }

    private fun MealNoteJpaEntity.toDomain() = MealNote(
        householdId = HouseholdId(householdId),
        date = date,
        note = note,
    )
}

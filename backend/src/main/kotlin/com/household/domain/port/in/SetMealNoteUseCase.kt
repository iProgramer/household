package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import java.time.LocalDate

data class SetMealNoteCommand(
    val householdId: HouseholdId,
    val date: LocalDate,
    val note: String,
)

interface SetMealNoteUseCase {
    fun set(command: SetMealNoteCommand): MealNote
}

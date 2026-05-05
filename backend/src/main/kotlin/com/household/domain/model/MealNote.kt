package com.household.domain.model

import java.time.LocalDate

data class MealNote(
    val householdId: HouseholdId,
    val date: LocalDate,
    val note: String,
)

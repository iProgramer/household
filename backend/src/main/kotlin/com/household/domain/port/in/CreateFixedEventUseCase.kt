package com.household.domain.port.`in`

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import com.household.domain.model.RecurrenceRule
import java.time.LocalDate

data class CreateFixedEventCommand(
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate,
    val recurrenceRule: RecurrenceRule? = null,
)

interface CreateFixedEventUseCase {
    fun create(command: CreateFixedEventCommand): FixedEvent
}

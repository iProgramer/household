package com.household.domain.port.`in`

import com.household.domain.model.FixedEvent
import com.household.domain.model.FixedEventId
import com.household.domain.model.RecurrenceRule
import java.time.LocalDate

data class UpdateFixedEventCommand(
    val id: FixedEventId,
    val title: String,
    val date: LocalDate,
    val recurrenceRule: RecurrenceRule,
)

interface UpdateFixedEventUseCase {
    fun update(command: UpdateFixedEventCommand): FixedEvent
}

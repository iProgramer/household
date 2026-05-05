package com.household.domain.port.`in`

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import java.time.LocalDate

interface GetFixedEventsForDateUseCase {
    fun getForDate(householdId: HouseholdId, date: LocalDate): List<FixedEvent>
}

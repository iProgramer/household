package com.household.domain.port.`in`

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import java.time.LocalDate

interface GetFixedEventsForWeekUseCase {
    fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<FixedEvent>
}

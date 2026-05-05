package com.household.domain.port.out

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId

interface FixedEventRepository {
    fun save(event: FixedEvent): FixedEvent
    fun findAllByHouseholdId(householdId: HouseholdId): List<FixedEvent>
}

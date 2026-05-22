package com.household.domain.port.out

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId

import com.household.domain.model.FixedEventId

interface FixedEventRepository {
    fun save(event: FixedEvent): FixedEvent
    fun findAllByHouseholdId(householdId: HouseholdId): List<FixedEvent>
    fun findById(id: FixedEventId): FixedEvent?
    fun delete(id: FixedEventId)
}

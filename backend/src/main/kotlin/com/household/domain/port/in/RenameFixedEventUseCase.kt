package com.household.domain.port.`in`

import com.household.domain.model.FixedEvent
import com.household.domain.model.FixedEventId

interface RenameFixedEventUseCase {
    fun rename(id: FixedEventId, title: String): FixedEvent
}

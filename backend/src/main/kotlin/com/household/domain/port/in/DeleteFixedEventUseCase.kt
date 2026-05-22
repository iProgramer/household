package com.household.domain.port.`in`

import com.household.domain.model.FixedEventId

interface DeleteFixedEventUseCase {
    fun delete(id: FixedEventId)
}

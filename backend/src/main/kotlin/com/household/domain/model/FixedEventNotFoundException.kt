package com.household.domain.model

class FixedEventNotFoundException(id: FixedEventId) : RuntimeException("FixedEvent not found: ${id.value}")

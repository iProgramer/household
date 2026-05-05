package com.household.domain.model

import java.time.LocalDate
import java.util.UUID

@JvmInline value class TaskId(val value: UUID)
@JvmInline value class HouseholdId(val value: UUID)

data class Task(
    val id: TaskId,
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate?,
    val status: TaskStatus,
) {
    companion object {
        fun create(householdId: HouseholdId, title: String, date: LocalDate?): Task {
            require(title.isNotBlank()) { "Title must not be blank" }
            return Task(
                id = TaskId(UUID.randomUUID()),
                householdId = householdId,
                title = title.trim(),
                date = date,
                status = TaskStatus.OPEN,
            )
        }
    }

    fun complete(): Task = copy(status = TaskStatus.DONE)
}

enum class TaskStatus { OPEN, DONE }

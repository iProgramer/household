package com.household.domain.port.`in`

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.ProjectId
import com.household.domain.model.RecurrenceRule
import com.household.domain.model.Task
import java.time.LocalDate

data class CreateTaskCommand(
    val householdId: HouseholdId,
    val title: String,
    val date: LocalDate?,
    val assignedTo: MemberId? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val projectId: ProjectId? = null,
)

interface CreateTaskUseCase {
    fun create(command: CreateTaskCommand): Task
}

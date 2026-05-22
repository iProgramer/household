package com.household.domain.port.`in`

import com.household.domain.model.MemberId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import java.time.LocalDate

data class UpdateTaskCommand(
    val taskId: TaskId,
    val date: LocalDate?,       // null = remove date (unplan)
    val assignedTo: MemberId?,  // null = unassign
    val title: String? = null,  // null = no change
)

interface UpdateTaskUseCase {
    fun update(command: UpdateTaskCommand): Task
}

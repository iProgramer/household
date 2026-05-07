package com.household.domain.port.`in`

import com.household.domain.model.Task
import com.household.domain.model.TaskId

interface ReopenTaskUseCase {
    fun reopen(taskId: TaskId): Task
}

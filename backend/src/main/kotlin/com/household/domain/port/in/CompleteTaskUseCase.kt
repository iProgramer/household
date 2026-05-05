package com.household.domain.port.`in`

import com.household.domain.model.Task
import com.household.domain.model.TaskId

interface CompleteTaskUseCase {
    fun complete(taskId: TaskId): Task
}

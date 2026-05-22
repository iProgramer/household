package com.household.domain.port.`in`

import com.household.domain.model.TaskId

interface DeleteTaskUseCase {
    fun delete(id: TaskId)
}

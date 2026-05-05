package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import java.time.LocalDate

interface TaskRepository {
    fun save(task: Task): Task
    fun findAllByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): List<Task>
    fun findById(id: TaskId): Task?
}

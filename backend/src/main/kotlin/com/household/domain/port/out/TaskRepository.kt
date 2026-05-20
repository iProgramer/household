package com.household.domain.port.out

import com.household.domain.model.HouseholdId
import com.household.domain.model.ProjectId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import java.time.LocalDate

interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: TaskId): Task?
    fun findAllByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): List<Task>
    fun findAllByHouseholdIdAndDateBetween(householdId: HouseholdId, start: LocalDate, end: LocalDate): List<Task>
    fun findAllOpenByHouseholdIdAndDateIsNull(householdId: HouseholdId): List<Task>
    fun findAllOpenByHouseholdIdAndDateBefore(householdId: HouseholdId, date: LocalDate): List<Task>
    fun findAllByProjectId(projectId: ProjectId): List<Task>
}

package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskStatus
import com.household.domain.port.out.TaskRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TaskRepositoryAdapter(
    private val jpa: TaskJpaRepository,
) : TaskRepository {

    override fun save(task: Task): Task =
        jpa.save(task.toJpaEntity()).toDomain()

    override fun findById(id: TaskId): Task? =
        jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAllByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): List<Task> =
        jpa.findAllByHouseholdIdAndDate(householdId.value, date).map { it.toDomain() }

    override fun findAllByHouseholdIdAndDateBetween(
        householdId: HouseholdId,
        start: LocalDate,
        end: LocalDate,
    ): List<Task> = jpa.findAllByHouseholdIdAndDateBetween(householdId.value, start, end).map { it.toDomain() }

    override fun findAllOpenByHouseholdIdAndDateIsNull(householdId: HouseholdId): List<Task> =
        jpa.findAllByHouseholdIdAndDateIsNullAndStatus(householdId.value, TaskStatus.OPEN.name)
            .map { it.toDomain() }

    private fun Task.toJpaEntity() = TaskJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        date = date,
        assignedTo = assignedTo?.value,
        status = status.name,
    )

    private fun TaskJpaEntity.toDomain() = Task(
        id = TaskId(id),
        householdId = HouseholdId(householdId),
        title = title,
        date = date,
        assignedTo = assignedTo?.let { MemberId(it) },
        status = TaskStatus.valueOf(status),
    )
}

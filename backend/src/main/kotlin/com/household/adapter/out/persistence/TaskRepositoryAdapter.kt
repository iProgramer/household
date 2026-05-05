package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.ProjectId
import com.household.domain.model.RecurrenceRule
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskStatus
import com.household.domain.port.out.TaskRepository
import org.springframework.stereotype.Component
import java.time.DayOfWeek
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

    override fun findAllByProjectId(projectId: ProjectId): List<Task> =
        jpa.findAllByProjectId(projectId.value).map { it.toDomain() }

    private fun Task.toJpaEntity() = TaskJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        date = date,
        assignedTo = assignedTo?.value,
        status = status.name,
        recurrenceType = recurrenceRule?.typeName(),
        recurrenceWeekday = (recurrenceRule as? RecurrenceRule.OnWeekday)?.dayOfWeek?.name,
        projectId = projectId?.value,
    )

    private fun TaskJpaEntity.toDomain() = Task(
        id = TaskId(id),
        householdId = HouseholdId(householdId),
        title = title,
        date = date,
        assignedTo = assignedTo?.let { MemberId(it) },
        status = TaskStatus.valueOf(status),
        recurrenceRule = toRecurrenceRule(recurrenceType, recurrenceWeekday),
        projectId = projectId?.let { ProjectId(it) },
    )

    private fun RecurrenceRule.typeName(): String = when (this) {
        is RecurrenceRule.Daily -> "DAILY"
        is RecurrenceRule.Weekly -> "WEEKLY"
        is RecurrenceRule.Biweekly -> "BIWEEKLY"
        is RecurrenceRule.Monthly -> "MONTHLY"
        is RecurrenceRule.OnWeekday -> "ON_WEEKDAY"
    }

    private fun toRecurrenceRule(type: String?, weekday: String?): RecurrenceRule? = when (type) {
        null -> null
        "DAILY" -> RecurrenceRule.Daily
        "WEEKLY" -> RecurrenceRule.Weekly
        "BIWEEKLY" -> RecurrenceRule.Biweekly
        "MONTHLY" -> RecurrenceRule.Monthly
        "ON_WEEKDAY" -> RecurrenceRule.OnWeekday(DayOfWeek.valueOf(requireNotNull(weekday)))
        else -> null
    }
}

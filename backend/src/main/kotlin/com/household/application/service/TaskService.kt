package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskNotFoundException
import com.household.domain.port.`in`.CompleteTaskUseCase
import com.household.domain.port.`in`.ReopenTaskUseCase
import com.household.domain.port.`in`.CreateTaskCommand
import com.household.domain.port.`in`.CreateTaskUseCase
import com.household.domain.port.`in`.GetTodayTasksUseCase
import com.household.domain.port.`in`.GetOverdueTasksUseCase
import com.household.domain.port.`in`.GetUnplannedTasksUseCase
import com.household.domain.port.`in`.GetWeekTasksUseCase
import com.household.domain.port.`in`.UpdateTaskCommand
import com.household.domain.port.`in`.UpdateTaskUseCase
import com.household.domain.port.out.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
) : CreateTaskUseCase, GetTodayTasksUseCase, GetWeekTasksUseCase, GetUnplannedTasksUseCase,
    GetOverdueTasksUseCase, CompleteTaskUseCase, ReopenTaskUseCase, UpdateTaskUseCase {

    override fun create(command: CreateTaskCommand): Task =
        taskRepository.save(Task.create(command.householdId, command.title, command.date, command.assignedTo, command.recurrenceRule, command.projectId))

    @Transactional(readOnly = true)
    override fun getTodayTasks(householdId: HouseholdId): List<Task> =
        taskRepository.findAllByHouseholdIdAndDate(householdId, LocalDate.now())

    @Transactional(readOnly = true)
    override fun getWeekTasks(householdId: HouseholdId, startDate: LocalDate): List<Task> =
        taskRepository.findAllByHouseholdIdAndDateBetween(householdId, startDate, startDate.plusDays(6))

    @Transactional(readOnly = true)
    override fun getUnplannedTasks(householdId: HouseholdId): List<Task> =
        taskRepository.findAllOpenByHouseholdIdAndDateIsNull(householdId)

    @Transactional(readOnly = true)
    override fun getOverdueTasks(householdId: HouseholdId): List<Task> =
        taskRepository.findAllOpenByHouseholdIdAndDateBefore(householdId, LocalDate.now())

    override fun complete(taskId: TaskId): Task {
        val task = taskRepository.findById(taskId) ?: throw TaskNotFoundException(taskId)
        val completed = taskRepository.save(task.complete())
        task.nextOccurrence()?.let { taskRepository.save(it) }
        return completed
    }

    override fun reopen(taskId: TaskId): Task {
        val task = taskRepository.findById(taskId) ?: throw TaskNotFoundException(taskId)
        return taskRepository.save(task.reopen())
    }

    override fun update(command: UpdateTaskCommand): Task {
        val task = taskRepository.findById(command.taskId) ?: throw TaskNotFoundException(command.taskId)
        return taskRepository.save(
            task.reschedule(command.date).reassign(command.assignedTo)
        )
    }
}

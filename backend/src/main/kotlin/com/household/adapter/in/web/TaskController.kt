package com.household.adapter.`in`.web

import com.household.domain.model.HouseholdId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CompleteTaskUseCase
import com.household.domain.port.`in`.CreateTaskCommand
import com.household.domain.port.`in`.CreateTaskUseCase
import com.household.domain.port.`in`.GetTodayTasksUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val createTask: CreateTaskUseCase,
    private val getTodayTasks: GetTodayTasksUseCase,
    private val completeTask: CompleteTaskUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid request: CreateTaskRequest): TaskResponse =
        TaskResponse.from(createTask.create(CreateTaskCommand(DEMO_HOUSEHOLD_ID, request.title, request.date)))

    @GetMapping("/today")
    fun getToday(): List<TaskResponse> =
        getTodayTasks.getTodayTasks(DEMO_HOUSEHOLD_ID).map(TaskResponse::from)

    @PostMapping("/{id}/complete")
    fun complete(@PathVariable id: UUID): TaskResponse =
        TaskResponse.from(completeTask.complete(TaskId(id)))

    companion object {
        // Iteration 1: kein Auth-Kontext, ein Demo-Haushalt
        val DEMO_HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

data class CreateTaskRequest(
    @field:NotBlank val title: String,
    val date: LocalDate? = null,
)

data class TaskResponse(
    val id: UUID,
    val title: String,
    val date: LocalDate?,
    val status: TaskStatus,
) {
    companion object {
        fun from(task: Task) = TaskResponse(
            id = task.id.value,
            title = task.title,
            date = task.date,
            status = task.status,
        )
    }
}

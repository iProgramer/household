package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.MemberId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskStatus
import com.household.domain.port.`in`.CompleteTaskUseCase
import com.household.domain.port.`in`.CreateTaskCommand
import com.household.domain.port.`in`.CreateTaskUseCase
import com.household.domain.port.`in`.GetTodayTasksUseCase
import com.household.domain.port.`in`.GetUnplannedTasksUseCase
import com.household.domain.port.`in`.GetWeekTasksUseCase
import com.household.domain.port.`in`.UpdateTaskCommand
import com.household.domain.port.`in`.UpdateTaskUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val createTask: CreateTaskUseCase,
    private val getTodayTasks: GetTodayTasksUseCase,
    private val getWeekTasks: GetWeekTasksUseCase,
    private val getUnplannedTasks: GetUnplannedTasksUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid request: CreateTaskRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): TaskResponse = TaskResponse.from(
        createTask.create(
            CreateTaskCommand(
                householdId = principal.householdId,
                title = request.title,
                date = request.date,
                assignedTo = request.assignedTo?.let { MemberId(it) },
            )
        )
    )

    @GetMapping("/today")
    fun getToday(@AuthenticationPrincipal principal: AuthenticatedMember): List<TaskResponse> =
        getTodayTasks.getTodayTasks(principal.householdId).map(TaskResponse::from)

    @GetMapping("/week")
    fun getWeek(
        @AuthenticationPrincipal principal: AuthenticatedMember,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
    ): List<TaskResponse> =
        getWeekTasks.getWeekTasks(principal.householdId, startDate).map(TaskResponse::from)

    @GetMapping("/unplanned")
    fun getUnplanned(@AuthenticationPrincipal principal: AuthenticatedMember): List<TaskResponse> =
        getUnplannedTasks.getUnplannedTasks(principal.householdId).map(TaskResponse::from)

    @PostMapping("/{id}/complete")
    fun complete(@PathVariable id: UUID): TaskResponse =
        TaskResponse.from(completeTask.complete(TaskId(id)))

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateTaskRequest,
    ): TaskResponse = TaskResponse.from(
        updateTask.update(
            UpdateTaskCommand(
                taskId = TaskId(id),
                date = request.date,
                assignedTo = request.assignedTo?.let { MemberId(it) },
            )
        )
    )
}

data class CreateTaskRequest(
    @field:NotBlank val title: String,
    val date: LocalDate? = null,
    val assignedTo: UUID? = null,
)

data class UpdateTaskRequest(
    val date: LocalDate?,
    val assignedTo: UUID? = null,
)

data class TaskResponse(
    val id: UUID,
    val title: String,
    val date: LocalDate?,
    val assignedTo: UUID?,
    val status: TaskStatus,
) {
    companion object {
        fun from(task: Task) = TaskResponse(
            id = task.id.value,
            title = task.title,
            date = task.date,
            assignedTo = task.assignedTo?.value,
            status = task.status,
        )
    }
}

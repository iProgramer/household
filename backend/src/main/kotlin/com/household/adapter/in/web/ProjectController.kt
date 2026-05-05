package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.Project
import com.household.domain.model.ProjectDetail
import com.household.domain.model.ProjectId
import com.household.domain.model.ProjectStatus
import com.household.domain.model.ProjectWithProgress
import com.household.domain.port.`in`.CompleteProjectUseCase
import com.household.domain.port.`in`.CreateProjectCommand
import com.household.domain.port.`in`.CreateProjectUseCase
import com.household.domain.port.`in`.GetProjectUseCase
import com.household.domain.port.`in`.GetProjectsUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val createProject: CreateProjectUseCase,
    private val getProjects: GetProjectsUseCase,
    private val getProject: GetProjectUseCase,
    private val completeProject: CompleteProjectUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid request: CreateProjectRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): ProjectResponse = ProjectResponse.from(
        createProject.create(
            CreateProjectCommand(
                householdId = principal.householdId,
                title = request.title,
                goal = request.goal,
            )
        )
    )

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthenticatedMember): List<ProjectWithProgressResponse> =
        getProjects.getProjects(principal.householdId).map(ProjectWithProgressResponse::from)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: UUID): ProjectDetailResponse =
        ProjectDetailResponse.from(getProject.getProject(ProjectId(id)))

    @PostMapping("/{id}/complete")
    fun complete(@PathVariable id: UUID): ProjectResponse =
        ProjectResponse.from(completeProject.complete(ProjectId(id)))
}

data class CreateProjectRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val goal: String,
)

data class ProjectResponse(
    val id: UUID,
    val title: String,
    val goal: String,
    val status: ProjectStatus,
) {
    companion object {
        fun from(project: Project) = ProjectResponse(
            id = project.id.value,
            title = project.title,
            goal = project.goal,
            status = project.status,
        )
    }
}

data class ProjectWithProgressResponse(
    val id: UUID,
    val title: String,
    val goal: String,
    val status: ProjectStatus,
    val totalSteps: Int,
    val completedSteps: Int,
) {
    companion object {
        fun from(p: ProjectWithProgress) = ProjectWithProgressResponse(
            id = p.project.id.value,
            title = p.project.title,
            goal = p.project.goal,
            status = p.project.status,
            totalSteps = p.totalSteps,
            completedSteps = p.completedSteps,
        )
    }
}

data class ProjectDetailResponse(
    val id: UUID,
    val title: String,
    val goal: String,
    val status: ProjectStatus,
    val tasks: List<TaskResponse>,
) {
    companion object {
        fun from(detail: ProjectDetail) = ProjectDetailResponse(
            id = detail.project.id.value,
            title = detail.project.title,
            goal = detail.project.goal,
            status = detail.project.status,
            tasks = detail.tasks.map(TaskResponse::from),
        )
    }
}

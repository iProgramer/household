package com.household.domain.port.`in`

import com.household.domain.model.ProjectDetail
import com.household.domain.model.ProjectId

interface GetProjectUseCase {
    fun getProject(projectId: ProjectId): ProjectDetail
}

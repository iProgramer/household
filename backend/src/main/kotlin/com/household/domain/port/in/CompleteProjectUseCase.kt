package com.household.domain.port.`in`

import com.household.domain.model.Project
import com.household.domain.model.ProjectId

interface CompleteProjectUseCase {
    fun complete(projectId: ProjectId): Project
}

package com.household.domain.model

class TaskNotFoundException(id: TaskId) : RuntimeException("Task not found: ${id.value}")

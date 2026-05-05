package com.household.adapter.`in`.web

import com.household.domain.model.TaskNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTaskNotFound(e: TaskNotFoundException): ErrorResponse =
        ErrorResponse(e.message ?: "Not found")

    data class ErrorResponse(val error: String)
}

package com.household.adapter.`in`.web

import com.household.domain.model.EmailAlreadyExistsException
import com.household.domain.model.HouseholdFullException
import com.household.domain.model.InvalidCredentialsException
import com.household.domain.model.InvalidInviteCodeException
import com.household.domain.model.ProjectNotFoundException
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

    @ExceptionHandler(ProjectNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProjectNotFound(e: ProjectNotFoundException): ErrorResponse =
        ErrorResponse(e.message ?: "Not found")

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException): ErrorResponse =
        ErrorResponse(e.message ?: "Unauthorized")

    @ExceptionHandler(InvalidInviteCodeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidInviteCode(e: InvalidInviteCodeException): ErrorResponse =
        ErrorResponse(e.message ?: "Invalid invite code")

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyExists(e: EmailAlreadyExistsException): ErrorResponse =
        ErrorResponse(e.message ?: "Conflict")

    @ExceptionHandler(HouseholdFullException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleHouseholdFull(e: HouseholdFullException): ErrorResponse =
        ErrorResponse(e.message ?: "Conflict")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(e: IllegalArgumentException): ErrorResponse =
        ErrorResponse(e.message ?: "Bad request")

    data class ErrorResponse(val error: String)
}

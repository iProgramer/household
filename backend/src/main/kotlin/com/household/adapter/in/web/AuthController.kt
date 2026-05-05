package com.household.adapter.`in`.web

import com.household.domain.model.Member
import com.household.domain.port.`in`.JoinHouseholdUseCase
import com.household.domain.port.`in`.LoginUseCase
import com.household.domain.port.`in`.RegisterUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val register: RegisterUseCase,
    private val join: JoinHouseholdUseCase,
    private val login: LoginUseCase,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody @Valid request: RegisterRequest): AuthResponse {
        val result = register.register(request.email, request.password)
        return AuthResponse.from(result.member, result.token, result.household.inviteCode)
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    fun join(@RequestBody @Valid request: JoinRequest): AuthResponse {
        val result = join.join(request.email, request.password, request.inviteCode)
        return AuthResponse.from(result.member, result.token, inviteCode = null)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): AuthResponse {
        val result = login.login(request.email, request.password)
        return AuthResponse.from(result.member, result.token, inviteCode = null)
    }
}

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
)

data class JoinRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank val inviteCode: String,
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

data class AuthResponse(
    val token: String,
    val memberId: UUID,
    val householdId: UUID,
    val inviteCode: String?,
) {
    companion object {
        fun from(member: Member, token: String, inviteCode: String?) = AuthResponse(
            token = token,
            memberId = member.id.value,
            householdId = member.householdId.value,
            inviteCode = inviteCode,
        )
    }
}

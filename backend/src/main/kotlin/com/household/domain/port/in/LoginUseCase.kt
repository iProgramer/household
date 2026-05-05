package com.household.domain.port.`in`

import com.household.domain.model.Member

data class LoginResult(val member: Member, val token: String)

interface LoginUseCase {
    fun login(email: String, password: String): LoginResult
}

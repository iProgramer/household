package com.household.domain.port.`in`

import com.household.domain.model.Household
import com.household.domain.model.Member

data class RegisterResult(val member: Member, val household: Household, val token: String)

interface RegisterUseCase {
    fun register(email: String, password: String): RegisterResult
}

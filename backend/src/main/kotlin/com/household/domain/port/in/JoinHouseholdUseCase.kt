package com.household.domain.port.`in`

import com.household.domain.model.Member

data class JoinResult(val member: Member, val token: String)

interface JoinHouseholdUseCase {
    fun join(email: String, password: String, inviteCode: String): JoinResult
}

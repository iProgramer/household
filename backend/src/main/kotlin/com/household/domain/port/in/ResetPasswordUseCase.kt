package com.household.domain.port.`in`

interface ResetPasswordUseCase {
    fun resetPassword(email: String, inviteCode: String, newPassword: String)
}

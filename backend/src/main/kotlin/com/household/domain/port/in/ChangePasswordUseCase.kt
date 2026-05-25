package com.household.domain.port.`in`

import com.household.domain.model.MemberId

interface ChangePasswordUseCase {
    fun changePassword(memberId: MemberId, currentPassword: String, newPassword: String)
}

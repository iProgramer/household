package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.port.`in`.GetInviteCodeUseCase
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/household")
class HouseholdController(
    private val getInviteCode: GetInviteCodeUseCase,
) {
    @GetMapping("/invite-code")
    fun inviteCode(@AuthenticationPrincipal principal: AuthenticatedMember): InviteCodeResponse =
        InviteCodeResponse(getInviteCode.getInviteCode(principal.householdId))
}

data class InviteCodeResponse(val inviteCode: String)

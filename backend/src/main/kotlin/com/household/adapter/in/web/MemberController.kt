package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.Member
import com.household.domain.port.`in`.GetMembersUseCase
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val getMembers: GetMembersUseCase,
) {
    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthenticatedMember): List<MemberResponse> =
        getMembers.getMembers(principal.householdId).map(MemberResponse::from)
}

data class MemberResponse(
    val id: UUID,
    val email: String,
) {
    companion object {
        fun from(member: Member) = MemberResponse(
            id = member.id.value,
            email = member.email,
        )
    }
}

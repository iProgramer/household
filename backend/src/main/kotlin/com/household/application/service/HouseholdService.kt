package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.port.`in`.GetInviteCodeUseCase
import com.household.domain.port.out.HouseholdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class HouseholdService(
    private val householdRepository: HouseholdRepository,
) : GetInviteCodeUseCase {

    override fun getInviteCode(householdId: HouseholdId): String =
        householdRepository.findById(householdId)?.inviteCode
            ?: error("Household not found: ${householdId.value}")
}

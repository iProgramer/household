package com.household.application.service

import com.household.adapter.`in`.web.security.JwtService
import com.household.domain.model.EmailAlreadyExistsException
import com.household.domain.model.Household
import com.household.domain.model.HouseholdFullException
import com.household.domain.model.InvalidCredentialsException
import com.household.domain.model.InvalidInviteCodeException
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import com.household.domain.port.`in`.ChangePasswordUseCase
import com.household.domain.port.`in`.JoinHouseholdUseCase
import com.household.domain.port.`in`.JoinResult
import com.household.domain.port.`in`.LoginResult
import com.household.domain.port.`in`.LoginUseCase
import com.household.domain.port.`in`.RegisterResult
import com.household.domain.port.`in`.RegisterUseCase
import com.household.domain.port.`in`.ResetPasswordUseCase
import com.household.domain.port.out.HouseholdRepository
import com.household.domain.port.out.MemberRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val memberRepository: MemberRepository,
    private val householdRepository: HouseholdRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) : RegisterUseCase, JoinHouseholdUseCase, LoginUseCase, ChangePasswordUseCase, ResetPasswordUseCase {

    override fun register(email: String, password: String): RegisterResult {
        val normalizedEmail = email.trim().lowercase()
        if (memberRepository.findByEmail(normalizedEmail) != null) {
            throw EmailAlreadyExistsException(normalizedEmail)
        }
        val household = householdRepository.save(Household.create())
        val member = memberRepository.save(
            Member.create(household.id, normalizedEmail, passwordEncoder.encode(password))
        )
        return RegisterResult(member, household, jwtService.generateToken(member))
    }

    override fun join(email: String, password: String, inviteCode: String): JoinResult {
        val normalizedEmail = email.trim().lowercase()
        if (memberRepository.findByEmail(normalizedEmail) != null) {
            throw EmailAlreadyExistsException(normalizedEmail)
        }
        val household = householdRepository.findByInviteCode(inviteCode.uppercase())
            ?: throw InvalidInviteCodeException(inviteCode)
        if (memberRepository.countByHouseholdId(household.id) >= 2) {
            throw HouseholdFullException()
        }
        val member = memberRepository.save(
            Member.create(household.id, normalizedEmail, passwordEncoder.encode(password))
        )
        return JoinResult(member, jwtService.generateToken(member))
    }

    override fun changePassword(memberId: MemberId, currentPassword: String, newPassword: String) {
        val member = memberRepository.findById(memberId) ?: throw InvalidCredentialsException()
        require(passwordEncoder.matches(currentPassword, member.passwordHash)) { "Aktuelles Passwort ist falsch" }
        require(newPassword.length >= 8) { "Neues Passwort muss mindestens 8 Zeichen lang sein" }
        memberRepository.save(member.withPasswordHash(passwordEncoder.encode(newPassword)))
    }

    override fun resetPassword(email: String, inviteCode: String, newPassword: String) {
        val member = memberRepository.findByEmail(email.trim().lowercase())
            ?: throw IllegalArgumentException("E-Mail oder Einladungscode ungültig")
        val household = householdRepository.findById(member.householdId)
            ?: throw IllegalArgumentException("E-Mail oder Einladungscode ungültig")
        require(household.inviteCode == inviteCode.uppercase()) { "E-Mail oder Einladungscode ungültig" }
        require(newPassword.length >= 8) { "Neues Passwort muss mindestens 8 Zeichen lang sein" }
        memberRepository.save(member.withPasswordHash(passwordEncoder.encode(newPassword)))
    }

    override fun login(email: String, password: String): LoginResult {
        val member = memberRepository.findByEmail(email.trim().lowercase())
            ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, member.passwordHash)) {
            throw InvalidCredentialsException()
        }
        return LoginResult(member, jwtService.generateToken(member))
    }
}

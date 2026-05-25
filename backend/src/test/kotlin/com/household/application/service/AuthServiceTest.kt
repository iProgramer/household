package com.household.application.service

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.domain.model.EmailAlreadyExistsException
import com.household.domain.model.Household
import com.household.domain.model.HouseholdFullException
import com.household.domain.model.HouseholdId
import com.household.domain.model.InvalidCredentialsException
import com.household.domain.model.InvalidInviteCodeException
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import com.household.domain.port.out.HouseholdRepository
import com.household.domain.port.out.MemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

class AuthServiceTest {

    private val memberRepository = mockk<MemberRepository>()
    private val householdRepository = mockk<HouseholdRepository>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val jwtService = mockk<JwtService>()
    private val service = AuthService(memberRepository, householdRepository, passwordEncoder, jwtService)

    @Test
    fun `register creates household and member, returns token`() {
        every { memberRepository.findByEmail("alice@example.com") } returns null
        every { householdRepository.save(any()) } answers { firstArg() }
        every { memberRepository.save(any()) } answers { firstArg() }
        every { jwtService.generateToken(any()) } returns "jwt-token"

        val result = service.register("alice@example.com", "password123")

        assertEquals("jwt-token", result.token)
        assertEquals("alice@example.com", result.member.email)
        verify { householdRepository.save(any()) }
        verify { memberRepository.save(any()) }
    }

    @Test
    fun `register normalizes email before duplicate check`() {
        every { memberRepository.findByEmail("alice@example.com") } returns null
        every { householdRepository.save(any()) } answers { firstArg() }
        every { memberRepository.save(any()) } answers { firstArg() }
        every { jwtService.generateToken(any()) } returns "token"

        service.register("  Alice@EXAMPLE.COM  ", "password123")

        verify { memberRepository.findByEmail("alice@example.com") }
    }

    @Test
    fun `register throws when email already exists`() {
        val existing = Member.create(HouseholdId(UUID.randomUUID()), "alice@example.com", "hash")
        every { memberRepository.findByEmail("alice@example.com") } returns existing

        assertThrows<EmailAlreadyExistsException> {
            service.register("alice@example.com", "password123")
        }
    }

    @Test
    fun `join finds household by invite code and creates member`() {
        val household = Household.create()
        every { memberRepository.findByEmail("bob@example.com") } returns null
        every { householdRepository.findByInviteCode(household.inviteCode) } returns household
        every { memberRepository.countByHouseholdId(household.id) } returns 1
        every { memberRepository.save(any()) } answers { firstArg() }
        every { jwtService.generateToken(any()) } returns "jwt-token"

        val result = service.join("bob@example.com", "password123", household.inviteCode)

        assertEquals("jwt-token", result.token)
        assertEquals(household.id, result.member.householdId)
    }

    @Test
    fun `join throws when invite code is invalid`() {
        every { memberRepository.findByEmail(any()) } returns null
        every { householdRepository.findByInviteCode("INVALID1") } returns null

        assertThrows<InvalidInviteCodeException> {
            service.join("bob@example.com", "password123", "INVALID1")
        }
    }

    @Test
    fun `join throws when household already has 2 members`() {
        val household = Household.create()
        every { memberRepository.findByEmail(any()) } returns null
        every { householdRepository.findByInviteCode(household.inviteCode) } returns household
        every { memberRepository.countByHouseholdId(household.id) } returns 2

        assertThrows<HouseholdFullException> {
            service.join("charlie@example.com", "password123", household.inviteCode)
        }
    }

    @Test
    fun `login returns token for valid credentials`() {
        val hash = passwordEncoder.encode("correct-password")
        val member = Member(MemberId(UUID.randomUUID()), HouseholdId(UUID.randomUUID()), "alice@example.com", hash)
        every { memberRepository.findByEmail("alice@example.com") } returns member
        every { jwtService.generateToken(member) } returns "jwt-token"

        val result = service.login("alice@example.com", "correct-password")

        assertEquals("jwt-token", result.token)
    }

    @Test
    fun `login throws for unknown email`() {
        every { memberRepository.findByEmail("unknown@example.com") } returns null

        assertThrows<InvalidCredentialsException> {
            service.login("unknown@example.com", "password123")
        }
    }

    @Test
    fun `login throws for wrong password`() {
        val hash = passwordEncoder.encode("correct-password")
        val member = Member(MemberId(UUID.randomUUID()), HouseholdId(UUID.randomUUID()), "alice@example.com", hash)
        every { memberRepository.findByEmail("alice@example.com") } returns member

        assertThrows<InvalidCredentialsException> {
            service.login("alice@example.com", "wrong-password")
        }
    }

    @Test
    fun `changePassword updates hash when current password is correct`() {
        val hash = passwordEncoder.encode("oldpass1")
        val member = Member(MemberId(UUID.randomUUID()), HouseholdId(UUID.randomUUID()), "alice@example.com", hash)
        every { memberRepository.findById(member.id) } returns member
        every { memberRepository.save(any()) } answers { firstArg() }

        service.changePassword(member.id, "oldpass1", "newpass123")

        verify { memberRepository.save(match { passwordEncoder.matches("newpass123", it.passwordHash) }) }
    }

    @Test
    fun `changePassword throws when current password is wrong`() {
        val hash = passwordEncoder.encode("correctpass")
        val member = Member(MemberId(UUID.randomUUID()), HouseholdId(UUID.randomUUID()), "alice@example.com", hash)
        every { memberRepository.findById(member.id) } returns member

        assertThrows<IllegalArgumentException> {
            service.changePassword(member.id, "wrongpass", "newpass123")
        }
    }

    @Test
    fun `resetPassword updates hash when email and invite code are valid`() {
        val household = Household.create()
        val hash = passwordEncoder.encode("oldpass1")
        val member = Member(MemberId(UUID.randomUUID()), household.id, "alice@example.com", hash)
        every { memberRepository.findByEmail("alice@example.com") } returns member
        every { householdRepository.findById(household.id) } returns household
        every { memberRepository.save(any()) } answers { firstArg() }

        service.resetPassword("alice@example.com", household.inviteCode, "newpass123")

        verify { memberRepository.save(match { passwordEncoder.matches("newpass123", it.passwordHash) }) }
    }

    @Test
    fun `resetPassword throws when invite code is wrong`() {
        val household = Household.create()
        val member = Member(MemberId(UUID.randomUUID()), household.id, "alice@example.com", "hash")
        every { memberRepository.findByEmail("alice@example.com") } returns member
        every { householdRepository.findById(household.id) } returns household

        assertThrows<IllegalArgumentException> {
            service.resetPassword("alice@example.com", "WRONG123", "newpass123")
        }
    }
}

package com.household.adapter.`in`.web

import com.household.domain.model.EmailAlreadyExistsException
import com.household.domain.model.Household
import com.household.domain.model.HouseholdId
import com.household.domain.model.InvalidCredentialsException
import com.household.domain.model.InvalidInviteCodeException
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import com.household.domain.port.`in`.JoinHouseholdUseCase
import com.household.domain.port.`in`.JoinResult
import com.household.domain.port.`in`.LoginResult
import com.household.domain.port.`in`.LoginUseCase
import com.household.domain.port.`in`.RegisterResult
import com.household.domain.port.`in`.RegisterUseCase
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(controllers = [AuthController::class])
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtService: JwtService  // needed by JwtAuthFilter

    @MockkBean
    lateinit var registerUseCase: RegisterUseCase

    @MockkBean
    lateinit var joinUseCase: JoinHouseholdUseCase

    @MockkBean
    lateinit var loginUseCase: LoginUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
    }

    private val householdId = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val memberId = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    private val member = Member(memberId, householdId, "alice@example.com", "hash")
    private val household = Household(householdId, "ABCD1234")

    @Test
    fun `POST register returns 201 with token and invite code`() {
        every { registerUseCase.register("alice@example.com", "password123") } returns
            RegisterResult(member, household, "jwt-token")

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "alice@example.com", "password": "password123"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { value("jwt-token") }
            jsonPath("$.inviteCode") { value("ABCD1234") }
            jsonPath("$.memberId") { exists() }
            jsonPath("$.householdId") { exists() }
        }
    }

    @Test
    fun `POST register with invalid email returns 400`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "not-an-email", "password": "password123"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST register with short password returns 400`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "alice@example.com", "password": "short"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST register with duplicate email returns 409`() {
        every { registerUseCase.register(any(), any()) } throws EmailAlreadyExistsException("alice@example.com")

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "alice@example.com", "password": "password123"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `POST join returns 201 without invite code`() {
        every { joinUseCase.join("bob@example.com", "password123", "ABCD1234") } returns
            JoinResult(member, "jwt-token")

        mockMvc.post("/api/auth/join") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "bob@example.com", "password": "password123", "inviteCode": "ABCD1234"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { value("jwt-token") }
            jsonPath("$.inviteCode") { value(null as String?) }
        }
    }

    @Test
    fun `POST join with invalid invite code returns 400`() {
        every { joinUseCase.join(any(), any(), "INVALID1") } throws InvalidInviteCodeException("INVALID1")

        mockMvc.post("/api/auth/join") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "bob@example.com", "password": "password123", "inviteCode": "INVALID1"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST login returns 200 with token`() {
        every { loginUseCase.login("alice@example.com", "password123") } returns
            LoginResult(member, "jwt-token")

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "alice@example.com", "password": "password123"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { value("jwt-token") }
        }
    }

    @Test
    fun `POST login with wrong credentials returns 401`() {
        every { loginUseCase.login(any(), any()) } throws InvalidCredentialsException()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "alice@example.com", "password": "wrong"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}

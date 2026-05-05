package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.RecurrenceRule
import com.household.domain.port.`in`.CreateFixedEventUseCase
import com.household.domain.port.`in`.GetFixedEventsForDateUseCase
import com.household.domain.port.`in`.GetFixedEventsForWeekUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(FixedEventController::class)
@Import(SecurityConfig::class)
class FixedEventControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var createFixedEvent: CreateFixedEventUseCase

    @MockkBean
    lateinit var getForDate: GetFixedEventsForDateUseCase

    @MockkBean
    lateinit var getForWeek: GetFixedEventsForWeekUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `POST creates fixed event and returns 201`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Müllabfuhr", LocalDate.of(2026, 5, 4))
        every { createFixedEvent.create(any()) } returns event

        mockMvc.post("/api/fixed-events") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Müllabfuhr", "date": "2026-05-04"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Müllabfuhr") }
            jsonPath("$.date") { value("2026-05-04") }
            jsonPath("$.id") { exists() }
            jsonPath("$.recurrence") { doesNotExist() }
        }
    }

    @Test
    fun `POST creates recurring fixed event and returns recurrence in response`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Gelber Sack", LocalDate.of(2026, 5, 4), RecurrenceRule.Weekly)
        every { createFixedEvent.create(any()) } returns event

        mockMvc.post("/api/fixed-events") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Gelber Sack", "date": "2026-05-04", "recurrence": {"type": "WEEKLY"}}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.recurrence.type") { value("WEEKLY") }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/fixed-events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Müllabfuhr", "date": "2026-05-04"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST with blank title returns 400`() {
        mockMvc.post("/api/fixed-events") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "", "date": "2026-05-04"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `GET today returns fixed events for today`() {
        val events = listOf(
            FixedEvent.create(HOUSEHOLD_ID, "Müllabfuhr", LocalDate.now()),
        )
        every { getForDate.getForDate(HOUSEHOLD_ID, LocalDate.now()) } returns events

        mockMvc.get("/api/fixed-events/today") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].title") { value("Müllabfuhr") }
        }
    }

    @Test
    fun `GET week returns fixed events for the week`() {
        val start = LocalDate.of(2026, 5, 4)
        val events = listOf(
            FixedEvent.create(HOUSEHOLD_ID, "Gelber Sack", start),
        )
        every { getForWeek.getForWeek(HOUSEHOLD_ID, start) } returns events

        mockMvc.get("/api/fixed-events/week?startDate=2026-05-04") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
        }
    }

    @Test
    fun `GET today without token returns 401`() {
        mockMvc.get("/api/fixed-events/today").andExpect {
            status { isUnauthorized() }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

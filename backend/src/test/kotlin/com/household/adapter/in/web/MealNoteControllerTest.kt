package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import com.household.domain.model.MemberId
import com.household.domain.port.`in`.GetMealNoteForDateUseCase
import com.household.domain.port.`in`.GetWeekMealNotesUseCase
import com.household.domain.port.`in`.SetMealNoteUseCase
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
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(MealNoteController::class)
@Import(SecurityConfig::class)
class MealNoteControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var setMealNote: SetMealNoteUseCase

    @MockkBean
    lateinit var getForDate: GetMealNoteForDateUseCase

    @MockkBean
    lateinit var getForWeek: GetWeekMealNotesUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `PUT sets meal note and returns it`() {
        val note = MealNote(HOUSEHOLD_ID, LocalDate.of(2026, 5, 9), "Pasta Bolognese")
        every { setMealNote.set(any()) } returns note

        mockMvc.put("/api/meal-notes/2026-05-09") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"note": "Pasta Bolognese"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.note") { value("Pasta Bolognese") }
            jsonPath("$.date") { value("2026-05-09") }
        }
    }

    @Test
    fun `PUT without token returns 401`() {
        mockMvc.put("/api/meal-notes/2026-05-09") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"note": "Pizza"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET today returns note when present`() {
        val note = MealNote(HOUSEHOLD_ID, LocalDate.now(), "Salat")
        every { getForDate.getForDate(HOUSEHOLD_ID, LocalDate.now()) } returns note

        mockMvc.get("/api/meal-notes/today") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.note") { value("Salat") }
        }
    }

    @Test
    fun `GET today returns 204 when no note`() {
        every { getForDate.getForDate(HOUSEHOLD_ID, LocalDate.now()) } returns null

        mockMvc.get("/api/meal-notes/today") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `GET week returns sparse list of notes`() {
        val start = LocalDate.of(2026, 5, 4)
        val notes = listOf(
            MealNote(HOUSEHOLD_ID, start, "Pizza"),
            MealNote(HOUSEHOLD_ID, start.plusDays(2), "Risotto"),
        )
        every { getForWeek.getForWeek(HOUSEHOLD_ID, start) } returns notes

        mockMvc.get("/api/meal-notes/week?startDate=2026-05-04") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].note") { value("Pizza") }
        }
    }

    @Test
    fun `GET today without token returns 401`() {
        mockMvc.get("/api/meal-notes/today").andExpect {
            status { isUnauthorized() }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

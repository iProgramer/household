package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MemberId
import com.household.domain.model.MealNotFoundException
import com.household.domain.port.`in`.AssignMealUseCase
import com.household.domain.port.`in`.CreateMealUseCase
import com.household.domain.port.`in`.DeleteMealUseCase
import com.household.domain.port.`in`.GetMealIdeasUseCase
import com.household.domain.port.`in`.GetMealsForDateUseCase
import com.household.domain.port.`in`.GetMealsForWeekUseCase
import com.household.domain.port.`in`.RenameMealUseCase
import com.household.domain.port.`in`.UnassignMealUseCase
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(MealController::class)
@Import(SecurityConfig::class)
class MealControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean lateinit var jwtService: JwtService
    @MockkBean lateinit var createMeal: CreateMealUseCase
    @MockkBean lateinit var getIdeas: GetMealIdeasUseCase
    @MockkBean lateinit var getForDate: GetMealsForDateUseCase
    @MockkBean lateinit var getForWeek: GetMealsForWeekUseCase
    @MockkBean lateinit var assignMeal: AssignMealUseCase
    @MockkBean lateinit var unassignMeal: UnassignMealUseCase
    @MockkBean lateinit var deleteMeal: DeleteMealUseCase
    @MockkBean lateinit var renameMeal: RenameMealUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `POST creates meal idea and returns 201`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Pasta")
        every { createMeal.create(any()) } returns meal

        mockMvc.post("/api/meals") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Pasta") }
            jsonPath("$.status") { value("IDEA") }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/meals") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET ideas returns list of IDEA meals`() {
        val ideas = listOf(
            Meal.createIdea(HOUSEHOLD_ID, "Pasta"),
            Meal.createIdea(HOUSEHOLD_ID, "Risotto"),
        )
        every { getIdeas.getIdeas(HOUSEHOLD_ID) } returns ideas

        mockMvc.get("/api/meals/ideas") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Pasta") }
        }
    }

    @Test
    fun `GET ideas without token returns 401`() {
        mockMvc.get("/api/meals/ideas").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `PUT assign sets date and returns PLANNED meal`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Pizza")
        val date = LocalDate.of(2026, 5, 20)
        val assigned = meal.assign(date)
        every { assignMeal.assign(any()) } returns assigned

        mockMvc.put("/api/meals/${meal.id.value}/assign") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-20"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PLANNED") }
            jsonPath("$.date") { value("2026-05-20") }
        }
    }

    @Test
    fun `PUT assign returns 404 when meal not found`() {
        every { assignMeal.assign(any()) } throws MealNotFoundException(MealId(UUID.randomUUID()))

        mockMvc.put("/api/meals/${UUID.randomUUID()}/assign") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-20"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT unassign returns IDEA meal`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Salat")
        every { unassignMeal.unassign(any()) } returns meal

        mockMvc.put("/api/meals/${meal.id.value}/unassign") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("IDEA") }
        }
    }

    @Test
    fun `PATCH renames meal and returns updated meal`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Pasta")
        val renamed = meal.rename("Pasta Bolognese")
        every { renameMeal.rename(meal.id, "Pasta Bolognese") } returns renamed

        mockMvc.patch("/api/meals/${meal.id.value}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta Bolognese"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Pasta Bolognese") }
        }
    }

    @Test
    fun `DELETE removes meal and returns 204`() {
        every { deleteMeal.delete(any()) } returns Unit

        mockMvc.delete("/api/meals/${UUID.randomUUID()}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `GET week returns planned meals for week`() {
        val monday = LocalDate.of(2026, 5, 18)
        val meals = listOf(Meal.createIdea(HOUSEHOLD_ID, "Pasta").assign(monday))
        every { getForWeek.getForWeek(HOUSEHOLD_ID, monday) } returns meals

        mockMvc.get("/api/meals/week?startDate=2026-05-18") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].status") { value("PLANNED") }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

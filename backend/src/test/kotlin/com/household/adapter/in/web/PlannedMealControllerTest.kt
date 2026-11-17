package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.PlannedMealNotFoundException
import com.household.domain.model.Recipe
import com.household.domain.port.`in`.GetPlannedMealForDateUseCase
import com.household.domain.port.`in`.GetPlannedMealsForWeekUseCase
import com.household.domain.port.`in`.GetRecipesUseCase
import com.household.domain.port.`in`.PlannedMealFavoriteResult
import com.household.domain.port.`in`.PlanMealUseCase
import com.household.domain.port.`in`.TogglePlannedMealFavoriteUseCase
import com.household.domain.port.`in`.UnplanMealUseCase
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
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(PlannedMealController::class)
@Import(SecurityConfig::class)
class PlannedMealControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean lateinit var jwtService: JwtService
    @MockkBean lateinit var planMeal: PlanMealUseCase
    @MockkBean lateinit var unplanMeal: UnplanMealUseCase
    @MockkBean lateinit var getForDate: GetPlannedMealForDateUseCase
    @MockkBean lateinit var getForWeek: GetPlannedMealsForWeekUseCase
    @MockkBean lateinit var togglePlannedMealFavorite: TogglePlannedMealFavoriteUseCase
    @MockkBean lateinit var getRecipes: GetRecipesUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
        every { getRecipes.getRecipes(HOUSEHOLD_ID) } returns emptyList()
    }

    @Test
    fun `POST plans meal from recipe and returns 201`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta").copy(favorite = true)
        val meal = PlannedMeal.fromRecipe(HOUSEHOLD_ID, MONDAY, recipe)
        every { planMeal.plan(any()) } returns meal
        every { getRecipes.getRecipes(HOUSEHOLD_ID) } returns listOf(recipe)

        mockMvc.post("/api/planned-meals") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-18", "recipeId": "${recipe.id.value}"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Pasta") }
            jsonPath("$.date") { value("2026-05-18") }
            jsonPath("$.recipeId") { value(recipe.id.value.toString()) }
            jsonPath("$.favorite") { value(true) }
        }
    }

    @Test
    fun `POST plans ad-hoc meal and returns 201`() {
        val meal = PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Reste")
        every { planMeal.plan(any()) } returns meal

        mockMvc.post("/api/planned-meals") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-18", "title": "Reste"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Reste") }
            jsonPath("$.recipeId") { isEmpty() }
            jsonPath("$.favorite") { isEmpty() }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/planned-meals") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-05-18", "title": "Test"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET date returns planned meal for date`() {
        val meal = PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Pasta")
        every { getForDate.getForDate(HOUSEHOLD_ID, MONDAY) } returns meal

        mockMvc.get("/api/planned-meals/date/2026-05-18") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Pasta") }
        }
    }

    @Test
    fun `GET date returns null when no meal planned`() {
        every { getForDate.getForDate(HOUSEHOLD_ID, MONDAY) } returns null

        mockMvc.get("/api/planned-meals/date/2026-05-18") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            content { string("") }
        }
    }

    @Test
    fun `GET week returns planned meals for week`() {
        val meals = listOf(PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Pasta"))
        every { getForWeek.getForWeek(HOUSEHOLD_ID, MONDAY) } returns meals

        mockMvc.get("/api/planned-meals/week?startDate=2026-05-18") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].title") { value("Pasta") }
        }
    }

    @Test
    fun `DELETE unplans meal and returns 204`() {
        every { unplanMeal.unplan(any()) } returns Unit

        mockMvc.delete("/api/planned-meals/${UUID.randomUUID()}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `DELETE returns 404 when meal not found`() {
        every { unplanMeal.unplan(any()) } throws PlannedMealNotFoundException(PlannedMealId(UUID.randomUUID()))

        mockMvc.delete("/api/planned-meals/${UUID.randomUUID()}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `POST toggle-favorite toggles planned meal favorite`() {
        val recipeId = UUID.randomUUID()
        every { togglePlannedMealFavorite.toggleFavorite(any(), HOUSEHOLD_ID) } returns
            PlannedMealFavoriteResult(recipeId, true)

        mockMvc.post("/api/planned-meals/${UUID.randomUUID()}/toggle-favorite") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.recipeId") { value(recipeId.toString()) }
            jsonPath("$.favorite") { value(true) }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
        val MONDAY: LocalDate = LocalDate.of(2026, 5, 18)
    }
}

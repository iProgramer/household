package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.adapter.`in`.web.security.JwtService
import com.household.adapter.`in`.web.security.SecurityConfig
import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.model.RecipeNotFoundException
import com.household.domain.port.`in`.CreateRecipeUseCase
import com.household.domain.port.`in`.DeleteRecipeUseCase
import com.household.domain.port.`in`.GetRecipesUseCase
import com.household.domain.port.`in`.RenameRecipeUseCase
import com.household.domain.port.`in`.ToggleFavoriteUseCase
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
import java.util.UUID

@WebMvcTest(RecipeController::class)
@Import(SecurityConfig::class)
class RecipeControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean lateinit var jwtService: JwtService
    @MockkBean lateinit var createRecipe: CreateRecipeUseCase
    @MockkBean lateinit var getRecipes: GetRecipesUseCase
    @MockkBean lateinit var renameRecipe: RenameRecipeUseCase
    @MockkBean lateinit var deleteRecipe: DeleteRecipeUseCase
    @MockkBean lateinit var toggleFavorite: ToggleFavoriteUseCase

    @BeforeEach
    fun setupAuth() {
        every { jwtService.extractPrincipal(any()) } returns null
        every { jwtService.extractPrincipal("valid-token") } returns AuthenticatedMember(
            memberId = MEMBER_ID,
            householdId = HOUSEHOLD_ID,
        )
    }

    @Test
    fun `POST creates recipe and returns 201`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta Bolognese")
        every { createRecipe.create(any()) } returns recipe

        mockMvc.post("/api/recipes") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta Bolognese"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Pasta Bolognese") }
            jsonPath("$.id") { isNotEmpty() }
            jsonPath("$.favorite") { value(false) }
        }
    }

    @Test
    fun `POST without token returns 401`() {
        mockMvc.post("/api/recipes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET returns list of recipes`() {
        val recipes = listOf(
            Recipe.create(HOUSEHOLD_ID, "Pasta"),
            Recipe.create(HOUSEHOLD_ID, "Pizza"),
        )
        every { getRecipes.getRecipes(HOUSEHOLD_ID) } returns recipes

        mockMvc.get("/api/recipes") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Pasta") }
        }
    }

    @Test
    fun `PATCH renames recipe`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta")
        val renamed = recipe.rename("Pasta Bolognese")
        every { renameRecipe.rename(recipe.id, "Pasta Bolognese") } returns renamed

        mockMvc.patch("/api/recipes/${recipe.id.value}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "Pasta Bolognese"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Pasta Bolognese") }
        }
    }

    @Test
    fun `PATCH returns 404 when recipe not found`() {
        every { renameRecipe.rename(any(), any()) } throws RecipeNotFoundException(RecipeId(UUID.randomUUID()))

        mockMvc.patch("/api/recipes/${UUID.randomUUID()}") {
            header("Authorization", "Bearer valid-token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title": "New name"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `DELETE removes recipe and returns 204`() {
        every { deleteRecipe.delete(any()) } returns Unit

        mockMvc.delete("/api/recipes/${UUID.randomUUID()}") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `POST toggle-favorite toggles recipe favorite`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta").toggleFavorite()
        every { toggleFavorite.toggleFavorite(recipe.id) } returns recipe

        mockMvc.post("/api/recipes/${recipe.id.value}/toggle-favorite") {
            header("Authorization", "Bearer valid-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.favorite") { value(true) }
        }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val MEMBER_ID = MemberId(UUID.fromString("00000000-0000-0000-0000-000000000010"))
    }
}

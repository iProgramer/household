package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.model.RecipeNotFoundException
import com.household.domain.port.`in`.CreateRecipeCommand
import com.household.domain.port.out.RecipeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RecipeServiceTest {

    private val recipeRepository = mockk<RecipeRepository>()
    private val service = RecipeService(recipeRepository)

    @Test
    fun `create saves recipe and returns it`() {
        val command = CreateRecipeCommand(HOUSEHOLD_ID, "Pasta Bolognese")
        every { recipeRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Pasta Bolognese", result.title)
        assertEquals(HOUSEHOLD_ID, result.householdId)
        verify(exactly = 1) { recipeRepository.save(any()) }
    }

    @Test
    fun `create trims whitespace from title`() {
        val command = CreateRecipeCommand(HOUSEHOLD_ID, "  Pizza  ")
        every { recipeRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Pizza", result.title)
    }

    @Test
    fun `getRecipes delegates to repository`() {
        every { recipeRepository.findAllByHouseholdId(HOUSEHOLD_ID) } returns emptyList()

        service.getRecipes(HOUSEHOLD_ID)

        verify { recipeRepository.findAllByHouseholdId(HOUSEHOLD_ID) }
    }

    @Test
    fun `rename updates recipe title`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta")
        every { recipeRepository.findById(recipe.id) } returns recipe
        every { recipeRepository.save(any()) } answers { firstArg() }

        val result = service.rename(recipe.id, "Pasta Bolognese")

        assertEquals("Pasta Bolognese", result.title)
        verify { recipeRepository.save(match { it.title == "Pasta Bolognese" }) }
    }

    @Test
    fun `rename throws RecipeNotFoundException when recipe does not exist`() {
        val unknownId = RecipeId(UUID.randomUUID())
        every { recipeRepository.findById(unknownId) } returns null

        assertThrows<RecipeNotFoundException> { service.rename(unknownId, "New title") }
    }

    @Test
    fun `delete delegates to repository`() {
        val id = RecipeId(UUID.randomUUID())
        every { recipeRepository.delete(id) } returns Unit

        service.delete(id)

        verify { recipeRepository.delete(id) }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.PlannedMealNotFoundException
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.model.RecipeNotFoundException
import com.household.domain.port.`in`.PlanMealCommand
import com.household.domain.port.out.PlannedMealRepository
import com.household.domain.port.out.RecipeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class PlannedMealServiceTest {

    private val plannedMealRepository = mockk<PlannedMealRepository>()
    private val recipeRepository = mockk<RecipeRepository>()
    private val service = PlannedMealService(plannedMealRepository, recipeRepository)

    @Test
    fun `plan from recipe creates planned meal with recipe title`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta Bolognese")
        val date = LocalDate.of(2026, 5, 20)
        every { recipeRepository.findById(recipe.id) } returns recipe
        every { plannedMealRepository.findByHouseholdIdAndDate(HOUSEHOLD_ID, date) } returns null
        every { plannedMealRepository.save(any()) } answers { firstArg() }

        val result = service.plan(PlanMealCommand(HOUSEHOLD_ID, date, recipe.id, null))

        assertEquals("Pasta Bolognese", result.title)
        assertEquals(date, result.date)
        assertEquals(recipe.id, result.recipeId)
        verify(exactly = 1) { plannedMealRepository.save(any()) }
    }

    @Test
    fun `plan ad-hoc creates planned meal without recipe`() {
        val date = LocalDate.of(2026, 5, 20)
        every { plannedMealRepository.findByHouseholdIdAndDate(HOUSEHOLD_ID, date) } returns null
        every { plannedMealRepository.save(any()) } answers { firstArg() }

        val result = service.plan(PlanMealCommand(HOUSEHOLD_ID, date, null, "Reste vom Vortag"))

        assertEquals("Reste vom Vortag", result.title)
        assertNull(result.recipeId)
    }

    @Test
    fun `plan replaces existing meal for same day`() {
        val date = LocalDate.of(2026, 5, 20)
        val existing = PlannedMeal.adHoc(HOUSEHOLD_ID, date, "Altes Gericht")
        every { plannedMealRepository.findByHouseholdIdAndDate(HOUSEHOLD_ID, date) } returns existing
        every { plannedMealRepository.delete(existing.id) } returns Unit
        every { plannedMealRepository.save(any()) } answers { firstArg() }

        val result = service.plan(PlanMealCommand(HOUSEHOLD_ID, date, null, "Neues Gericht"))

        assertEquals("Neues Gericht", result.title)
        verify { plannedMealRepository.delete(existing.id) }
    }

    @Test
    fun `plan throws RecipeNotFoundException for unknown recipe`() {
        val unknownId = RecipeId(UUID.randomUUID())
        every { recipeRepository.findById(unknownId) } returns null

        assertThrows<RecipeNotFoundException> {
            service.plan(PlanMealCommand(HOUSEHOLD_ID, LocalDate.now(), unknownId, null))
        }
    }

    @Test
    fun `plan throws IllegalArgumentException when neither recipeId nor title provided`() {
        assertThrows<IllegalArgumentException> {
            service.plan(PlanMealCommand(HOUSEHOLD_ID, LocalDate.now(), null, null))
        }
    }

    @Test
    fun `plan throws IllegalArgumentException when title is blank`() {
        assertThrows<IllegalArgumentException> {
            service.plan(PlanMealCommand(HOUSEHOLD_ID, LocalDate.now(), null, "   "))
        }
    }

    @Test
    fun `unplan deletes planned meal`() {
        val meal = PlannedMeal.adHoc(HOUSEHOLD_ID, LocalDate.now(), "Test")
        every { plannedMealRepository.findById(meal.id) } returns meal
        every { plannedMealRepository.delete(meal.id) } returns Unit

        service.unplan(meal.id)

        verify { plannedMealRepository.delete(meal.id) }
    }

    @Test
    fun `unplan throws PlannedMealNotFoundException for unknown id`() {
        val unknownId = PlannedMealId(UUID.randomUUID())
        every { plannedMealRepository.findById(unknownId) } returns null

        assertThrows<PlannedMealNotFoundException> { service.unplan(unknownId) }
    }

    @Test
    fun `getForDate delegates to repository`() {
        val date = LocalDate.of(2026, 5, 20)
        every { plannedMealRepository.findByHouseholdIdAndDate(HOUSEHOLD_ID, date) } returns null

        val result = service.getForDate(HOUSEHOLD_ID, date)

        assertNull(result)
        verify { plannedMealRepository.findByHouseholdIdAndDate(HOUSEHOLD_ID, date) }
    }

    @Test
    fun `getForWeek delegates with correct date range`() {
        val start = LocalDate.of(2026, 5, 18)
        every { plannedMealRepository.findByHouseholdIdBetween(HOUSEHOLD_ID, start, start.plusDays(6)) } returns emptyList()

        service.getForWeek(HOUSEHOLD_ID, start)

        verify { plannedMealRepository.findByHouseholdIdBetween(HOUSEHOLD_ID, start, start.plusDays(6)) }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MealNotFoundException
import com.household.domain.model.MealStatus
import com.household.domain.port.`in`.AssignMealCommand
import com.household.domain.port.`in`.CreateMealCommand
import com.household.domain.port.out.MealRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class MealServiceTest {

    private val mealRepository = mockk<MealRepository>()
    private val service = MealService(mealRepository)

    @Test
    fun `create saves idea and returns it`() {
        val command = CreateMealCommand(HOUSEHOLD_ID, "Pasta Bolognese")
        every { mealRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Pasta Bolognese", result.title)
        assertEquals(MealStatus.IDEA, result.status)
        assertEquals(HOUSEHOLD_ID, result.householdId)
        verify(exactly = 1) { mealRepository.save(any()) }
    }

    @Test
    fun `create trims whitespace from title`() {
        val command = CreateMealCommand(HOUSEHOLD_ID, "  Pizza  ")
        every { mealRepository.save(any()) } answers { firstArg() }

        val result = service.create(command)

        assertEquals("Pizza", result.title)
    }

    @Test
    fun `getIdeas delegates to repository`() {
        every { mealRepository.findIdeasByHouseholdId(HOUSEHOLD_ID) } returns emptyList()

        service.getIdeas(HOUSEHOLD_ID)

        verify { mealRepository.findIdeasByHouseholdId(HOUSEHOLD_ID) }
    }

    @Test
    fun `assign finds meal, sets date and status, saves it`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Risotto")
        val date = LocalDate.of(2026, 5, 20)
        every { mealRepository.findById(meal.id) } returns meal
        every { mealRepository.save(any()) } answers { firstArg() }

        val result = service.assign(AssignMealCommand(meal.id, date))

        assertEquals(MealStatus.PLANNED, result.status)
        assertEquals(date, result.date)
        verify { mealRepository.save(match { it.status == MealStatus.PLANNED && it.date == date }) }
    }

    @Test
    fun `assign throws MealNotFoundException when meal does not exist`() {
        val unknownId = MealId(UUID.randomUUID())
        every { mealRepository.findById(unknownId) } returns null

        assertThrows<MealNotFoundException> {
            service.assign(AssignMealCommand(unknownId, LocalDate.now()))
        }
    }

    @Test
    fun `unassign finds meal, clears date and status, saves it`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Salat").assign(LocalDate.now())
        every { mealRepository.findById(meal.id) } returns meal
        every { mealRepository.save(any()) } answers { firstArg() }

        val result = service.unassign(meal.id)

        assertEquals(MealStatus.IDEA, result.status)
        assertEquals(null, result.date)
    }

    @Test
    fun `unassign throws MealNotFoundException when meal does not exist`() {
        val unknownId = MealId(UUID.randomUUID())
        every { mealRepository.findById(unknownId) } returns null

        assertThrows<MealNotFoundException> { service.unassign(unknownId) }
    }

    @Test
    fun `delete delegates to repository`() {
        val id = MealId(UUID.randomUUID())
        every { mealRepository.delete(id) } returns Unit

        service.delete(id)

        verify { mealRepository.delete(id) }
    }

    @Test
    fun `getForWeek delegates with correct date range`() {
        val start = LocalDate.of(2026, 5, 18)
        every { mealRepository.findPlannedByHouseholdIdBetween(HOUSEHOLD_ID, start, start.plusDays(6)) } returns emptyList()

        service.getForWeek(HOUSEHOLD_ID, start)

        verify { mealRepository.findPlannedByHouseholdIdBetween(HOUSEHOLD_ID, start, start.plusDays(6)) }
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

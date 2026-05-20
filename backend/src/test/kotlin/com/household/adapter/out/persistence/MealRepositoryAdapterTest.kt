package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MealStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
class MealRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: MealJpaRepository

    lateinit var adapter: MealRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = MealRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve idea by id`() {
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Pasta")

        val saved = adapter.save(meal)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("Pasta", found!!.title)
        assertEquals(MealStatus.IDEA, found.status)
        assertNull(found.date)
    }

    @Test
    fun `save and retrieve planned meal with date`() {
        val date = LocalDate.of(2026, 5, 20)
        val meal = Meal.createIdea(HOUSEHOLD_ID, "Pizza").assign(date)

        val saved = adapter.save(meal)
        val found = adapter.findById(saved.id)!!

        assertEquals(MealStatus.PLANNED, found.status)
        assertEquals(date, found.date)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = adapter.findById(MealId(UUID.randomUUID()))
        assertNull(result)
    }

    @Test
    fun `findIdeasByHouseholdId returns only IDEA meals`() {
        val date = LocalDate.of(2026, 5, 20)
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Idee A"))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Idee B").assign(date))

        val result = adapter.findIdeasByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Idee A", result[0].title)
    }

    @Test
    fun `findIdeasByHouseholdId excludes other households`() {
        val otherHousehold = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Unser Haushalt"))
        adapter.save(Meal.createIdea(otherHousehold, "Anderer Haushalt"))

        val result = adapter.findIdeasByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Unser Haushalt", result[0].title)
    }

    @Test
    fun `findPlannedByHouseholdIdAndDate returns meals for that date only`() {
        val monday = LocalDate.of(2026, 5, 18)
        val tuesday = monday.plusDays(1)
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Montag").assign(monday))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Dienstag").assign(tuesday))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Noch Idee"))

        val result = adapter.findPlannedByHouseholdIdAndDate(HOUSEHOLD_ID, monday)

        assertEquals(1, result.size)
        assertEquals("Montag", result[0].title)
    }

    @Test
    fun `findPlannedByHouseholdIdBetween returns meals within range`() {
        val monday = LocalDate.of(2026, 5, 18)
        val sunday = monday.plusDays(6)
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Diese Woche").assign(monday))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Nächste Woche").assign(sunday.plusDays(1)))
        adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Noch Idee"))

        val result = adapter.findPlannedByHouseholdIdBetween(HOUSEHOLD_ID, monday, sunday)

        assertEquals(1, result.size)
        assertEquals("Diese Woche", result[0].title)
    }

    @Test
    fun `delete removes meal`() {
        val meal = adapter.save(Meal.createIdea(HOUSEHOLD_ID, "Zu löschen"))

        adapter.delete(meal.id)

        assertNull(adapter.findById(meal.id))
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

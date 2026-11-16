package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.Recipe
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
class PlannedMealRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: PlannedMealJpaRepository

    lateinit var adapter: PlannedMealRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = PlannedMealRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve planned meal by id`() {
        val meal = PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Reste")

        val saved = adapter.save(meal)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("Reste", found!!.title)
        assertEquals(MONDAY, found.date)
        assertNull(found.recipeId)
    }

    @Test
    fun `save and retrieve planned meal with recipeId`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta")
        val meal = PlannedMeal.fromRecipe(HOUSEHOLD_ID, MONDAY, recipe)

        val saved = adapter.save(meal)
        val found = adapter.findById(saved.id)!!

        assertEquals("Pasta", found.title)
        assertEquals(recipe.id, found.recipeId)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = adapter.findById(PlannedMealId(UUID.randomUUID()))
        assertNull(result)
    }

    @Test
    fun `findByHouseholdIdAndDate returns meal for that date`() {
        adapter.save(PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Montag"))
        adapter.save(PlannedMeal.adHoc(HOUSEHOLD_ID, TUESDAY, "Dienstag"))

        val result = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, MONDAY)

        assertNotNull(result)
        assertEquals("Montag", result!!.title)
    }

    @Test
    fun `findByHouseholdIdAndDate returns null when no meal for date`() {
        val result = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, MONDAY)
        assertNull(result)
    }

    @Test
    fun `findByHouseholdIdAndDate excludes other households`() {
        adapter.save(PlannedMeal.adHoc(OTHER_HOUSEHOLD_ID, MONDAY, "Anderer"))

        val result = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, MONDAY)

        assertNull(result)
    }

    @Test
    fun `findByHouseholdIdBetween returns meals within range`() {
        adapter.save(PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Diese Woche"))
        adapter.save(PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY.plusDays(7), "Nächste Woche"))

        val result = adapter.findByHouseholdIdBetween(HOUSEHOLD_ID, MONDAY, MONDAY.plusDays(6))

        assertEquals(1, result.size)
        assertEquals("Diese Woche", result[0].title)
    }

    @Test
    fun `delete removes planned meal`() {
        val meal = adapter.save(PlannedMeal.adHoc(HOUSEHOLD_ID, MONDAY, "Zu löschen"))

        adapter.delete(meal.id)

        assertNull(adapter.findById(meal.id))
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val OTHER_HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        val MONDAY: LocalDate = LocalDate.of(2026, 5, 18)
        val TUESDAY: LocalDate = MONDAY.plusDays(1)
    }
}

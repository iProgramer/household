package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.UUID

@DataJpaTest
class RecipeRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: RecipeJpaRepository

    lateinit var adapter: RecipeRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = RecipeRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve recipe by id`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Pasta Bolognese")

        val saved = adapter.save(recipe)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("Pasta Bolognese", found!!.title)
        assertEquals(HOUSEHOLD_ID, found.householdId)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = adapter.findById(RecipeId(UUID.randomUUID()))
        assertNull(result)
    }

    @Test
    fun `findAllByHouseholdId returns recipes for household`() {
        adapter.save(Recipe.create(HOUSEHOLD_ID, "Pasta"))
        adapter.save(Recipe.create(HOUSEHOLD_ID, "Pizza"))
        adapter.save(Recipe.create(OTHER_HOUSEHOLD_ID, "Anderer Haushalt"))

        val result = adapter.findAllByHouseholdId(HOUSEHOLD_ID)

        assertEquals(2, result.size)
    }

    @Test
    fun `save and retrieve preserves favorite flag`() {
        val recipe = Recipe.create(HOUSEHOLD_ID, "Favorit").toggleFavorite()

        val saved = adapter.save(recipe)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals(true, found!!.favorite)
    }

    @Test
    fun `delete removes recipe`() {
        val recipe = adapter.save(Recipe.create(HOUSEHOLD_ID, "Zu löschen"))

        adapter.delete(recipe.id)

        assertNull(adapter.findById(recipe.id))
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val OTHER_HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
    }
}

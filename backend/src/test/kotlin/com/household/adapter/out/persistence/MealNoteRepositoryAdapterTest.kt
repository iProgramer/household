package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
class MealNoteRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: MealNoteJpaRepository

    lateinit var adapter: MealNoteRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = MealNoteRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve by date`() {
        val note = MealNote(HOUSEHOLD_ID, LocalDate.of(2026, 5, 9), "Pasta")

        adapter.save(note)
        val found = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, LocalDate.of(2026, 5, 9))

        assertEquals("Pasta", found?.note)
    }

    @Test
    fun `save twice for same date updates note (upsert)`() {
        val date = LocalDate.of(2026, 5, 9)
        adapter.save(MealNote(HOUSEHOLD_ID, date, "Pasta"))
        adapter.save(MealNote(HOUSEHOLD_ID, date, "Pizza"))

        val found = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, date)

        assertEquals("Pizza", found?.note)
        assertEquals(1, jpa.findAll().size)
    }

    @Test
    fun `findByHouseholdIdAndDate returns null when absent`() {
        val result = adapter.findByHouseholdIdAndDate(HOUSEHOLD_ID, LocalDate.of(2026, 5, 9))
        assertNull(result)
    }

    @Test
    fun `findAllByHouseholdIdAndDateBetween returns notes in range`() {
        val monday = LocalDate.of(2026, 5, 4)
        adapter.save(MealNote(HOUSEHOLD_ID, monday, "Pizza"))
        adapter.save(MealNote(HOUSEHOLD_ID, monday.plusDays(3), "Salat"))
        adapter.save(MealNote(HOUSEHOLD_ID, monday.plusDays(7), "Nächste Woche"))

        val result = adapter.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, monday, monday.plusDays(6))

        assertEquals(2, result.size)
    }

    @Test
    fun `findAllByHouseholdIdAndDateBetween excludes other households`() {
        val other = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        val date = LocalDate.of(2026, 5, 4)
        adapter.save(MealNote(HOUSEHOLD_ID, date, "Unser Essen"))
        adapter.save(MealNote(other, date, "Anderes Essen"))

        val result = adapter.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, date, date.plusDays(6))

        assertEquals(1, result.size)
        assertEquals("Unser Essen", result[0].note)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

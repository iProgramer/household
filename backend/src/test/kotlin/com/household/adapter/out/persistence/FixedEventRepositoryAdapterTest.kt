package com.household.adapter.out.persistence

import com.household.domain.model.FixedEvent
import com.household.domain.model.HouseholdId
import com.household.domain.model.RecurrenceRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
class FixedEventRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: FixedEventJpaRepository

    lateinit var adapter: FixedEventRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = FixedEventRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve by household`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Müllabfuhr", LocalDate.of(2026, 5, 4))

        adapter.save(event)
        val found = adapter.findAllByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, found.size)
        assertEquals("Müllabfuhr", found[0].title)
    }

    @Test
    fun `findAllByHouseholdId excludes other households`() {
        val other = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        adapter.save(FixedEvent.create(HOUSEHOLD_ID, "Unser Termin", LocalDate.now()))
        adapter.save(FixedEvent.create(other, "Anderer Termin", LocalDate.now()))

        val result = adapter.findAllByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Unser Termin", result[0].title)
    }

    @Test
    fun `save and retrieve without recurrence`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Einmalig", LocalDate.now())
        adapter.save(event)

        val found = adapter.findAllByHouseholdId(HOUSEHOLD_ID)[0]
        assertNull(found.recurrenceRule)
    }

    @Test
    fun `save and retrieve with weekly recurrence`() {
        val event = FixedEvent.create(HOUSEHOLD_ID, "Gelber Sack", LocalDate.now(), RecurrenceRule.Weekly)
        adapter.save(event)

        val found = adapter.findAllByHouseholdId(HOUSEHOLD_ID)[0]
        assertEquals(RecurrenceRule.Weekly, found.recurrenceRule)
    }

    @Test
    fun `save and retrieve with OnWeekday recurrence`() {
        val rule = RecurrenceRule.OnWeekday(DayOfWeek.TUESDAY)
        val event = FixedEvent.create(HOUSEHOLD_ID, "Markt", LocalDate.now(), rule)
        adapter.save(event)

        val found = adapter.findAllByHouseholdId(HOUSEHOLD_ID)[0]
        assertEquals(rule, found.recurrenceRule)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.MemberId
import com.household.domain.model.Task
import com.household.domain.model.TaskId
import com.household.domain.model.TaskStatus
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
class TaskRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: TaskJpaRepository

    lateinit var adapter: TaskRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = TaskRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve task by id`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", LocalDate.now())

        val saved = adapter.save(task)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("Staubsaugen", found!!.title)
        assertEquals(TaskStatus.OPEN, found.status)
        assertEquals(HOUSEHOLD_ID, found.householdId)
    }

    @Test
    fun `save task without date`() {
        val task = Task.create(HOUSEHOLD_ID, "Ohne Datum", null)

        val saved = adapter.save(task)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertNull(found!!.date)
    }

    @Test
    fun `save completed task preserves status`() {
        val task = Task.create(HOUSEHOLD_ID, "Staubsaugen", null).complete()

        val saved = adapter.save(task)
        val found = adapter.findById(saved.id)

        assertEquals(TaskStatus.DONE, found!!.status)
    }

    @Test
    fun `findAllByHouseholdIdAndDate returns only tasks for given date`() {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        adapter.save(Task.create(HOUSEHOLD_ID, "Heute", today))
        adapter.save(Task.create(HOUSEHOLD_ID, "Morgen", tomorrow))
        adapter.save(Task.create(HOUSEHOLD_ID, "Ohne Datum", null))

        val result = adapter.findAllByHouseholdIdAndDate(HOUSEHOLD_ID, today)

        assertEquals(1, result.size)
        assertEquals("Heute", result[0].title)
    }

    @Test
    fun `findAllByHouseholdIdAndDate excludes other households`() {
        val today = LocalDate.now()
        val otherHousehold = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

        adapter.save(Task.create(HOUSEHOLD_ID, "Unser Haushalt", today))
        adapter.save(Task.create(otherHousehold, "Anderer Haushalt", today))

        val result = adapter.findAllByHouseholdIdAndDate(HOUSEHOLD_ID, today)

        assertEquals(1, result.size)
        assertEquals("Unser Haushalt", result[0].title)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = adapter.findById(TaskId(UUID.randomUUID()))

        assertNull(result)
    }

    @Test
    fun `findAllByHouseholdIdAndDateBetween returns tasks within range`() {
        val monday = LocalDate.of(2026, 5, 4)
        val sunday = monday.plusDays(6)

        adapter.save(Task.create(HOUSEHOLD_ID, "Montag", monday))
        adapter.save(Task.create(HOUSEHOLD_ID, "Sonntag", sunday))
        adapter.save(Task.create(HOUSEHOLD_ID, "Nächste Woche", sunday.plusDays(1)))
        adapter.save(Task.create(HOUSEHOLD_ID, "Ohne Datum", null))

        val result = adapter.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, monday, sunday)

        assertEquals(2, result.size)
        val titles = result.map { it.title }
        assert("Montag" in titles)
        assert("Sonntag" in titles)
    }

    @Test
    fun `findAllByHouseholdIdAndDateBetween excludes other households`() {
        val start = LocalDate.of(2026, 5, 4)
        val otherHousehold = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

        adapter.save(Task.create(HOUSEHOLD_ID, "Unser Haushalt", start))
        adapter.save(Task.create(otherHousehold, "Anderer Haushalt", start))

        val result = adapter.findAllByHouseholdIdAndDateBetween(HOUSEHOLD_ID, start, start.plusDays(6))

        assertEquals(1, result.size)
        assertEquals("Unser Haushalt", result[0].title)
    }

    @Test
    fun `findAllOpenByHouseholdIdAndDateIsNull returns only open tasks without date`() {
        adapter.save(Task.create(HOUSEHOLD_ID, "Ohne Datum offen", null))
        adapter.save(Task.create(HOUSEHOLD_ID, "Mit Datum", LocalDate.now()))
        adapter.save(Task.create(HOUSEHOLD_ID, "Erledigt ohne Datum", null).complete())

        val result = adapter.findAllOpenByHouseholdIdAndDateIsNull(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Ohne Datum offen", result[0].title)
        assertEquals(TaskStatus.OPEN, result[0].status)
        assertNull(result[0].date)
    }

    @Test
    fun `findAllOpenByHouseholdIdAndDateIsNull excludes other households`() {
        val otherHousehold = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

        adapter.save(Task.create(HOUSEHOLD_ID, "Unser Haushalt", null))
        adapter.save(Task.create(otherHousehold, "Anderer Haushalt", null))

        val result = adapter.findAllOpenByHouseholdIdAndDateIsNull(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Unser Haushalt", result[0].title)
    }

    @Test
    fun `save preserves assignment after update`() {
        val memberId = MemberId(UUID.randomUUID())
        val task = Task.create(HOUSEHOLD_ID, "Aufgabe", null)
        val saved = adapter.save(task)

        val reassigned = adapter.save(saved.reassign(memberId))

        assertEquals(memberId, reassigned.assignedTo)
        assertEquals(memberId, adapter.findById(saved.id)!!.assignedTo)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Project
import com.household.domain.model.ProjectStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.UUID

@DataJpaTest
class ProjectRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: ProjectJpaRepository

    lateinit var adapter: ProjectRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = ProjectRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve by id`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig")

        val saved = adapter.save(project)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("Keller", found!!.title)
        assertEquals(ProjectStatus.ACTIVE, found.status)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = adapter.findById(com.household.domain.model.ProjectId(UUID.randomUUID()))
        assertNull(result)
    }

    @Test
    fun `save completed project preserves status`() {
        val project = Project.create(HOUSEHOLD_ID, "Keller", "Fertig").complete()
        val saved = adapter.save(project)

        assertEquals(ProjectStatus.DONE, adapter.findById(saved.id)!!.status)
    }

    @Test
    fun `findAllByHouseholdId returns projects for household`() {
        adapter.save(Project.create(HOUSEHOLD_ID, "Projekt A", "Ziel A"))
        adapter.save(Project.create(HOUSEHOLD_ID, "Projekt B", "Ziel B"))

        val result = adapter.findAllByHouseholdId(HOUSEHOLD_ID)

        assertEquals(2, result.size)
    }

    @Test
    fun `findAllByHouseholdId excludes other households`() {
        val other = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        adapter.save(Project.create(HOUSEHOLD_ID, "Unser Projekt", "Ziel"))
        adapter.save(Project.create(other, "Anderes Projekt", "Ziel"))

        val result = adapter.findAllByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, result.size)
        assertEquals("Unser Projekt", result[0].title)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

package com.household.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class ProjectTest {

    private val householdId = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    @Test
    fun `create with valid data`() {
        val project = Project.create(householdId, "Keller aufräumen", "Alles in Kisten sortiert")

        assertEquals("Keller aufräumen", project.title)
        assertEquals("Alles in Kisten sortiert", project.goal)
        assertEquals(ProjectStatus.ACTIVE, project.status)
        assertEquals(householdId, project.householdId)
    }

    @Test
    fun `create trims whitespace from title and goal`() {
        val project = Project.create(householdId, "  Keller  ", "  Fertig  ")

        assertEquals("Keller", project.title)
        assertEquals("Fertig", project.goal)
    }

    @Test
    fun `create with blank title throws`() {
        assertThrows<IllegalArgumentException> {
            Project.create(householdId, "   ", "Ziel")
        }
    }

    @Test
    fun `create with blank goal throws`() {
        assertThrows<IllegalArgumentException> {
            Project.create(householdId, "Titel", "   ")
        }
    }

    @Test
    fun `complete changes status to DONE`() {
        val project = Project.create(householdId, "Keller", "Fertig")

        val completed = project.complete()

        assertEquals(ProjectStatus.DONE, completed.status)
    }

    @Test
    fun `complete returns new instance, original unchanged`() {
        val project = Project.create(householdId, "Keller", "Fertig")

        val completed = project.complete()

        assertEquals(ProjectStatus.ACTIVE, project.status)
        assertEquals(ProjectStatus.DONE, completed.status)
    }

    @Test
    fun `each created project has a unique id`() {
        val a = Project.create(householdId, "Projekt A", "Ziel A")
        val b = Project.create(householdId, "Projekt B", "Ziel B")

        assert(a.id != b.id)
    }
}

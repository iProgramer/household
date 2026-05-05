package com.household.scenario

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `create project, add tasks, track progress, complete project`() {
        val token = registerAndGetToken("project-${System.nanoTime()}@example.com")

        // Projekt anlegen
        val createResponse = restTemplate.postForEntity(
            "/api/projects",
            HttpEntity(
                mapOf("title" to "Keller aufräumen", "goal" to "Alles in Kisten sortiert"),
                authHeaders(token),
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val projectId = createResponse.body!!["id"] as String
        assertEquals("ACTIVE", createResponse.body!!["status"])

        // Teilschritte hinzufügen
        repeat(3) { i ->
            val taskResponse = restTemplate.postForEntity(
                "/api/tasks",
                HttpEntity(
                    mapOf("title" to "Schritt ${i + 1}", "projectId" to projectId),
                    authHeaders(token),
                ),
                Map::class.java,
            )
            assertEquals(HttpStatus.CREATED, taskResponse.statusCode)
            assertEquals(projectId, taskResponse.body!!["projectId"])
        }

        // Projektliste prüfen (3 Schritte, 0 erledigt)
        val listResponse = restTemplate.exchange(
            "/api/projects", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val project = (listResponse.body!! as List<Map<*, *>>).first { it["id"] == projectId }
        assertEquals(3, project["totalSteps"])
        assertEquals(0, project["completedSteps"])

        // Einen Schritt erledigen
        val detailResponse = restTemplate.exchange(
            "/api/projects/$projectId", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val firstTaskId = ((detailResponse.body!!["tasks"] as List<Map<*, *>>)[0]["id"]) as String
        restTemplate.postForEntity(
            "/api/tasks/$firstTaskId/complete",
            HttpEntity<Void>(authHeaders(token)),
            Map::class.java,
        )

        // Fortschritt aktualisiert
        val updatedListResponse = restTemplate.exchange(
            "/api/projects", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val updatedProject = (updatedListResponse.body!! as List<Map<*, *>>).first { it["id"] == projectId }
        assertEquals(1, updatedProject["completedSteps"])

        // Projekt abschließen
        val completeResponse = restTemplate.postForEntity(
            "/api/projects/$projectId/complete",
            HttpEntity<Void>(authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, completeResponse.statusCode)
        assertEquals("DONE", completeResponse.body!!["status"])
    }

    @Test
    fun `complete unknown project returns 404`() {
        val token = registerAndGetToken("proj-404-${System.nanoTime()}@example.com")
        val response = restTemplate.postForEntity(
            "/api/projects/00000000-0000-0000-0000-000000000099/complete",
            HttpEntity<Void>(authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    private fun registerAndGetToken(email: String): String {
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            mapOf("email" to email, "password" to "password123"),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body!!["token"] as String
    }

    private fun authHeaders(token: String) = HttpHeaders().apply { setBearerAuth(token) }
}

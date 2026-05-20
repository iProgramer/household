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
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MealScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `create idea, assign to day, find in week view, then unassign back to pool`() {
        val token = registerAndGetToken("meal-scenario-${System.nanoTime()}@example.com")
        val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

        // 1. Idee erstellen
        val createResponse = restTemplate.postForEntity(
            "/api/meals",
            HttpEntity(mapOf("title" to "Pasta Bolognese"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val mealId = createResponse.body!!["id"] as String
        assertEquals("IDEA", createResponse.body!!["status"])

        // 2. In der Ideen-Liste prüfen
        val ideasResponse = restTemplate.exchange(
            "/api/meals/ideas", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        assertEquals(HttpStatus.OK, ideasResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val ideaTitles = (ideasResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Pasta Bolognese" in ideaTitles)

        // 3. Auf Montag einplanen
        val assignResponse = restTemplate.exchange(
            "/api/meals/$mealId/assign", HttpMethod.PUT,
            HttpEntity(mapOf("date" to monday.toString()), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, assignResponse.statusCode)
        assertEquals("PLANNED", assignResponse.body!!["status"])
        assertEquals(monday.toString(), assignResponse.body!!["date"])

        // 4. Nicht mehr in der Ideen-Liste
        val ideasAfterResponse = restTemplate.exchange(
            "/api/meals/ideas", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val ideaTitlesAfter = (ideasAfterResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Pasta Bolognese" !in ideaTitlesAfter)

        // 5. In der Wochenansicht erscheinen
        val weekResponse = restTemplate.exchange(
            "/api/meals/week?startDate=$monday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        assertEquals(HttpStatus.OK, weekResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val weekTitles = (weekResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Pasta Bolognese" in weekTitles)

        // 6. Zurück in den Pool (unassign)
        val unassignResponse = restTemplate.exchange(
            "/api/meals/$mealId/unassign", HttpMethod.PUT,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals(HttpStatus.OK, unassignResponse.statusCode)
        assertEquals("IDEA", unassignResponse.body!!["status"])

        // 7. Wieder in der Ideen-Liste
        val ideasFinalResponse = restTemplate.exchange(
            "/api/meals/ideas", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val ideaFinalTitles = (ideasFinalResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Pasta Bolognese" in ideaFinalTitles)
    }

    @Test
    fun `delete idea removes it from pool`() {
        val token = registerAndGetToken("meal-delete-${System.nanoTime()}@example.com")

        val createResponse = restTemplate.postForEntity(
            "/api/meals",
            HttpEntity(mapOf("title" to "Zu löschen"), authHeaders(token)),
            Map::class.java,
        )
        val mealId = createResponse.body!!["id"] as String

        val deleteResponse = restTemplate.exchange(
            "/api/meals/$mealId", HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(token)), Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.statusCode)

        val ideasResponse = restTemplate.exchange(
            "/api/meals/ideas", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val titles = (ideasResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Zu löschen" !in titles)
    }

    @Test
    fun `assign returns 404 for unknown meal`() {
        val token = registerAndGetToken("meal-404-${System.nanoTime()}@example.com")

        val response = restTemplate.exchange(
            "/api/meals/00000000-0000-0000-0000-000000000099/assign", HttpMethod.PUT,
            HttpEntity(mapOf("date" to LocalDate.now().toString()), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `accessing meals without token returns 401`() {
        val response = restTemplate.getForEntity("/api/meals/ideas", Map::class.java)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
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

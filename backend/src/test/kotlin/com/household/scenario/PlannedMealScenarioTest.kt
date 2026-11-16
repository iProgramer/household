package com.household.scenario

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class PlannedMealScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `plan recipe for day, verify recipe stays in library, then unplan`() {
        val token = registerAndGetToken("planned-meal-recipe-${System.nanoTime()}@example.com")
        val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

        // 1. Rezept erstellen
        val createResponse = restTemplate.postForEntity(
            "/api/recipes",
            HttpEntity(mapOf("title" to "Gefüllte Wraps"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val recipeId = createResponse.body!!["id"] as String

        // 2. Rezept für Montag einplanen
        val planResponse = restTemplate.postForEntity(
            "/api/planned-meals",
            HttpEntity(mapOf("date" to monday.toString(), "recipeId" to recipeId), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, planResponse.statusCode)
        val plannedMealId = planResponse.body!!["id"] as String
        assertEquals("Gefüllte Wraps", planResponse.body!!["title"])
        assertEquals(recipeId, planResponse.body!!["recipeId"])

        // 3. Rezept ist noch in der Bibliothek (Kernunterschied zum alten Verhalten!)
        val recipesResponse = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val recipeTitles = (recipesResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Gefüllte Wraps" in recipeTitles)

        // 4. In der Wochenansicht erscheinen
        val weekResponse = restTemplate.exchange(
            "/api/planned-meals/week?startDate=$monday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        assertEquals(HttpStatus.OK, weekResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val weekTitles = (weekResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Gefüllte Wraps" in weekTitles)

        // 5. Am Tagesdatum abrufbar
        val dateResponse = restTemplate.exchange(
            "/api/planned-meals/date/$monday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals(HttpStatus.OK, dateResponse.statusCode)
        assertEquals("Gefüllte Wraps", dateResponse.body!!["title"])

        // 6. Entplanen
        val unplanResponse = restTemplate.exchange(
            "/api/planned-meals/$plannedMealId", HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(token)), Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, unplanResponse.statusCode)

        // 7. Rezept ist immer noch in der Bibliothek
        val recipesAfterResponse = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val recipeTitlesAfter = (recipesAfterResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Gefüllte Wraps" in recipeTitlesAfter)
    }

    @Test
    fun `plan ad-hoc meal without recipe`() {
        val token = registerAndGetToken("planned-meal-adhoc-${System.nanoTime()}@example.com")
        val tuesday = LocalDate.now().with(java.time.DayOfWeek.TUESDAY)

        // Ad-hoc Meal einplanen
        val planResponse = restTemplate.postForEntity(
            "/api/planned-meals",
            HttpEntity(mapOf("date" to tuesday.toString(), "title" to "Reste vom Vortag"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, planResponse.statusCode)
        assertEquals("Reste vom Vortag", planResponse.body!!["title"])
        assertEquals(null, planResponse.body!!["recipeId"])

        // Abrufbar am Tag
        val dateResponse = restTemplate.exchange(
            "/api/planned-meals/date/$tuesday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals("Reste vom Vortag", dateResponse.body!!["title"])
    }

    @Test
    fun `planning a second meal replaces the first`() {
        val token = registerAndGetToken("planned-meal-replace-${System.nanoTime()}@example.com")
        val wednesday = LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY)

        // Erstes Meal
        restTemplate.postForEntity(
            "/api/planned-meals",
            HttpEntity(mapOf("date" to wednesday.toString(), "title" to "Erstes Gericht"), authHeaders(token)),
            Map::class.java,
        )

        // Zweites Meal am selben Tag
        restTemplate.postForEntity(
            "/api/planned-meals",
            HttpEntity(mapOf("date" to wednesday.toString(), "title" to "Zweites Gericht"), authHeaders(token)),
            Map::class.java,
        )

        // Nur das zweite ist da
        val dateResponse = restTemplate.exchange(
            "/api/planned-meals/date/$wednesday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals("Zweites Gericht", dateResponse.body!!["title"])
    }

    @Test
    fun `accessing planned meals without token returns 401`() {
        val response = restTemplate.getForEntity("/api/planned-meals/date/${LocalDate.now()}", Map::class.java)
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

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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MealNoteScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `set meal note for today and retrieve it`() {
        val token = registerAndGetToken("meal-${System.nanoTime()}@example.com")
        val today = LocalDate.now()

        // Note setzen
        val putResponse = restTemplate.exchange(
            "/api/meal-notes/$today", HttpMethod.PUT,
            HttpEntity(mapOf("note" to "Pasta Bolognese"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, putResponse.statusCode)
        assertEquals("Pasta Bolognese", putResponse.body!!["note"])

        // Heute abrufen
        val todayResponse = restTemplate.exchange(
            "/api/meal-notes/today", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals(HttpStatus.OK, todayResponse.statusCode)
        assertEquals("Pasta Bolognese", todayResponse.body!!["note"])
    }

    @Test
    fun `update existing meal note (upsert)`() {
        val token = registerAndGetToken("meal-update-${System.nanoTime()}@example.com")
        val today = LocalDate.now()

        restTemplate.exchange("/api/meal-notes/$today", HttpMethod.PUT,
            HttpEntity(mapOf("note" to "Pizza"), authHeaders(token)), Map::class.java)
        restTemplate.exchange("/api/meal-notes/$today", HttpMethod.PUT,
            HttpEntity(mapOf("note" to "Risotto"), authHeaders(token)), Map::class.java)

        val response = restTemplate.exchange(
            "/api/meal-notes/today", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Map::class.java,
        )
        assertEquals("Risotto", response.body!!["note"])
    }

    @Test
    fun `get today returns 204 when no note set`() {
        val token = registerAndGetToken("meal-empty-${System.nanoTime()}@example.com")

        val response = restTemplate.exchange(
            "/api/meal-notes/today", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `week view returns sparse notes for set days only`() {
        val token = registerAndGetToken("meal-week-${System.nanoTime()}@example.com")
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        restTemplate.exchange("/api/meal-notes/$monday", HttpMethod.PUT,
            HttpEntity(mapOf("note" to "Montag-Essen"), authHeaders(token)), Map::class.java)
        restTemplate.exchange("/api/meal-notes/${monday.plusDays(4)}", HttpMethod.PUT,
            HttpEntity(mapOf("note" to "Freitag-Essen"), authHeaders(token)), Map::class.java)

        val weekResponse = restTemplate.exchange(
            "/api/meal-notes/week?startDate=$monday", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        assertEquals(HttpStatus.OK, weekResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val notes = (weekResponse.body!! as List<Map<*, *>>).map { it["note"] }
        assertEquals(2, notes.size)
        assertTrue("Montag-Essen" in notes)
        assertTrue("Freitag-Essen" in notes)
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

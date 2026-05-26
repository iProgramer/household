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
class FixedEventScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `create recurring fixed event and verify it appears in week view`() {
        val token = registerAndGetToken("events-${System.nanoTime()}@example.com")

        // Gelber Sack jeden Montag, startend diese Woche
        val thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val createResponse = restTemplate.postForEntity(
            "/api/fixed-events",
            HttpEntity(
                mapOf(
                    "title" to "Gelber Sack",
                    "date" to thisMonday.toString(),
                    "recurrence" to mapOf("type" to "WEEKLY"),
                ),
                authHeaders(token),
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertEquals("WEEKLY", (createResponse.body!!["recurrence"] as Map<*, *>)["type"])

        // In der Wochenansicht dieser Woche erscheinen
        val weekResponse = restTemplate.exchange(
            "/api/fixed-events/week?startDate=$thisMonday",
            HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)),
            List::class.java,
        )
        assertEquals(HttpStatus.OK, weekResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val titles = (weekResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Gelber Sack" in titles)

        // Auch nächste Woche erscheinen
        val nextWeekResponse = restTemplate.exchange(
            "/api/fixed-events/week?startDate=${thisMonday.plusWeeks(1)}",
            HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)),
            List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val nextWeekTitles = (nextWeekResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Gelber Sack" in nextWeekTitles)
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

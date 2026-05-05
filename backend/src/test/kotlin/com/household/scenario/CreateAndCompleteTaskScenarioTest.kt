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
class CreateAndCompleteTaskScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `register, create task, verify in today list, then complete it`() {
        val token = registerAndGetToken("alice-${System.nanoTime()}@example.com")

        // 1. Task anlegen
        val createRequest = mapOf("title" to "Bad putzen", "date" to LocalDate.now().toString())
        val createResponse = restTemplate.postForEntity(
            "/api/tasks", HttpEntity(createRequest, authHeaders(token)), Map::class.java
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val taskId = createResponse.body!!["id"] as String
        assertEquals("Bad putzen", createResponse.body!!["title"])
        assertEquals("OPEN", createResponse.body!!["status"])

        // 2. In der Tagesliste prüfen (GET mit Auth-Header via exchange)
        val todayResponse = restTemplate.exchange(
            "/api/tasks/today", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java
        )
        assertEquals(HttpStatus.OK, todayResponse.statusCode)

        @Suppress("UNCHECKED_CAST")
        val titles = (todayResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Bad putzen" in titles)

        // 3. Task abschließen
        val completeResponse = restTemplate.postForEntity(
            "/api/tasks/$taskId/complete", HttpEntity<Void>(authHeaders(token)), Map::class.java
        )
        assertEquals(HttpStatus.OK, completeResponse.statusCode)
        assertEquals("DONE", completeResponse.body!!["status"])
    }

    @Test
    fun `two members share the same household and tasks`() {
        // Person A registriert sich
        val registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            mapOf("email" to "personA-${System.nanoTime()}@example.com", "password" to "password123"),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)
        val tokenA = registerResponse.body!!["token"] as String
        val inviteCode = registerResponse.body!!["inviteCode"] as String

        // Person B tritt bei
        val joinResponse = restTemplate.postForEntity(
            "/api/auth/join",
            mapOf(
                "email" to "personB-${System.nanoTime()}@example.com",
                "password" to "password123",
                "inviteCode" to inviteCode,
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, joinResponse.statusCode)
        val tokenB = joinResponse.body!!["token"] as String
        val memberBId = joinResponse.body!!["memberId"] as String

        // A legt Task an, weist ihn B zu
        val createResponse = restTemplate.postForEntity(
            "/api/tasks",
            HttpEntity(
                mapOf("title" to "Einkaufen", "date" to LocalDate.now().toString(), "assignedTo" to memberBId),
                authHeaders(tokenA)
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertEquals(memberBId, createResponse.body!!["assignedTo"])

        // B sieht den Task in seiner Tagesliste
        val todayResponse = restTemplate.exchange(
            "/api/tasks/today", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(tokenB)), List::class.java
        )
        @Suppress("UNCHECKED_CAST")
        val titles = (todayResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Einkaufen" in titles)
    }

    @Test
    fun `complete unknown task returns 404`() {
        val token = registerAndGetToken("user-404-${System.nanoTime()}@example.com")
        val response = restTemplate.postForEntity(
            "/api/tasks/00000000-0000-0000-0000-000000000099/complete",
            HttpEntity<Void>(authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `accessing tasks without token returns 401`() {
        val response = restTemplate.getForEntity("/api/tasks/today", Map::class.java)
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

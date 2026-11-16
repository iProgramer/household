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
class RecipeScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `create recipe, list it, rename it, delete it`() {
        val token = registerAndGetToken("recipe-crud-${System.nanoTime()}@example.com")

        // 1. Rezept erstellen
        val createResponse = restTemplate.postForEntity(
            "/api/recipes",
            HttpEntity(mapOf("title" to "Pasta Bolognese"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val recipeId = createResponse.body!!["id"] as String
        assertEquals("Pasta Bolognese", createResponse.body!!["title"])

        // 2. In der Liste prüfen
        val listResponse = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        assertEquals(HttpStatus.OK, listResponse.statusCode)
        @Suppress("UNCHECKED_CAST")
        val titles = (listResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Pasta Bolognese" in titles)

        // 3. Umbenennen
        val renameResponse = restTemplate.exchange(
            "/api/recipes/$recipeId", HttpMethod.PATCH,
            HttpEntity(mapOf("title" to "Spaghetti Bolognese"), authHeaders(token)),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, renameResponse.statusCode)
        assertEquals("Spaghetti Bolognese", renameResponse.body!!["title"])

        // 4. Löschen
        val deleteResponse = restTemplate.exchange(
            "/api/recipes/$recipeId", HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(token)), Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.statusCode)

        // 5. Nicht mehr in der Liste
        val listAfterResponse = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(token)), List::class.java,
        )
        @Suppress("UNCHECKED_CAST")
        val titlesAfter = (listAfterResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Spaghetti Bolognese" !in titlesAfter)
    }

    @Test
    fun `accessing recipes without token returns 401`() {
        val response = restTemplate.getForEntity("/api/recipes", Map::class.java)
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

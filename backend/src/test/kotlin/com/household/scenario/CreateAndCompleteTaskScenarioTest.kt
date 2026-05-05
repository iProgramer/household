package com.household.scenario

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAndCompleteTaskScenarioTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `create task, verify in today list, then complete it`() {
        // 1. Task anlegen
        val createRequest = mapOf("title" to "Bad putzen", "date" to LocalDate.now().toString())
        val createResponse = restTemplate.postForEntity("/api/tasks", createRequest, Map::class.java)

        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val taskId = createResponse.body!!["id"] as String
        assertNotNull(taskId)
        assertEquals("Bad putzen", createResponse.body!!["title"])
        assertEquals("OPEN", createResponse.body!!["status"])

        // 2. In der Tagesliste prüfen
        val todayResponse = restTemplate.getForEntity("/api/tasks/today", List::class.java)
        assertEquals(HttpStatus.OK, todayResponse.statusCode)

        @Suppress("UNCHECKED_CAST")
        val titles = (todayResponse.body!! as List<Map<*, *>>).map { it["title"] }
        assertTrue("Bad putzen" in titles)

        // 3. Task abschließen
        val completeResponse = restTemplate.postForEntity("/api/tasks/$taskId/complete", null, Map::class.java)
        assertEquals(HttpStatus.OK, completeResponse.statusCode)
        assertEquals("DONE", completeResponse.body!!["status"])
        assertEquals(taskId, completeResponse.body!!["id"])
    }

    @Test
    fun `complete unknown task returns 404`() {
        val fakeId = "00000000-0000-0000-0000-000000000099"
        val response = restTemplate.postForEntity("/api/tasks/$fakeId/complete", null, Map::class.java)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `create task with blank title returns 400`() {
        val request = mapOf("title" to "")
        val response = restTemplate.postForEntity("/api/tasks", request, Map::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}

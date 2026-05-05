package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.UUID

@DataJpaTest
class MemberRepositoryAdapterTest {

    @Autowired
    lateinit var jpa: MemberJpaRepository

    lateinit var adapter: MemberRepositoryAdapter

    @BeforeEach
    fun setup() {
        adapter = MemberRepositoryAdapter(jpa)
    }

    @Test
    fun `save and retrieve member by id`() {
        val member = Member.create(HOUSEHOLD_ID, "alice@example.com", "hash")

        val saved = adapter.save(member)
        val found = adapter.findById(saved.id)

        assertNotNull(found)
        assertEquals("alice@example.com", found!!.email)
        assertEquals(HOUSEHOLD_ID, found.householdId)
    }

    @Test
    fun `findByEmail returns member for known email`() {
        adapter.save(Member.create(HOUSEHOLD_ID, "alice@example.com", "hash"))

        val found = adapter.findByEmail("alice@example.com")

        assertNotNull(found)
        assertEquals("alice@example.com", found!!.email)
    }

    @Test
    fun `findByEmail returns null for unknown email`() {
        val found = adapter.findByEmail("unknown@example.com")

        assertNull(found)
    }

    @Test
    fun `countByHouseholdId returns correct count`() {
        adapter.save(Member.create(HOUSEHOLD_ID, "alice@example.com", "hash"))
        adapter.save(Member.create(HOUSEHOLD_ID, "bob@example.com", "hash"))

        val count = adapter.countByHouseholdId(HOUSEHOLD_ID)

        assertEquals(2, count)
    }

    @Test
    fun `countByHouseholdId excludes other households`() {
        val otherHousehold = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        adapter.save(Member.create(HOUSEHOLD_ID, "alice@example.com", "hash"))
        adapter.save(Member.create(otherHousehold, "bob@example.com", "hash"))

        val count = adapter.countByHouseholdId(HOUSEHOLD_ID)

        assertEquals(1, count)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val found = adapter.findById(MemberId(UUID.randomUUID()))

        assertNull(found)
    }

    companion object {
        val HOUSEHOLD_ID = HouseholdId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    }
}

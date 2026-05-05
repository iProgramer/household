package com.household.adapter.`in`.web.security

import com.household.domain.model.HouseholdId
import com.household.domain.model.Member
import com.household.domain.model.MemberId
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long,
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8)) }

    fun generateToken(member: Member): String =
        Jwts.builder()
            .subject(member.id.value.toString())
            .claim("householdId", member.householdId.value.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()

    fun extractPrincipal(token: String): AuthenticatedMember? =
        try {
            val claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).payload
            AuthenticatedMember(
                memberId = MemberId(UUID.fromString(claims.subject)),
                householdId = HouseholdId(UUID.fromString(claims.get("householdId", String::class.java))),
            )
        } catch (_: JwtException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}

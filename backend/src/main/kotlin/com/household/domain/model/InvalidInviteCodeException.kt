package com.household.domain.model

class InvalidInviteCodeException(code: String) : RuntimeException("Invalid invite code: $code")

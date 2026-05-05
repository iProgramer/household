package com.household.domain.model

class EmailAlreadyExistsException(email: String) : RuntimeException("Email already registered: $email")

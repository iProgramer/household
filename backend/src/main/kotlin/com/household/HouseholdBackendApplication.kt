package com.household

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HouseholdBackendApplication

fun main(args: Array<String>) {
	runApplication<HouseholdBackendApplication>(*args)
}

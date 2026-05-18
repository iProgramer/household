package com.household.domain.port.`in`

import com.household.domain.model.Meal
import com.household.domain.model.MealId
import java.time.LocalDate

data class AssignMealCommand(val id: MealId, val date: LocalDate)

interface AssignMealUseCase {
    fun assign(command: AssignMealCommand): Meal
}

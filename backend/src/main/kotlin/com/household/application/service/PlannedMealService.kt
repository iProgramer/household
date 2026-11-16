package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.PlannedMealNotFoundException
import com.household.domain.model.RecipeId
import com.household.domain.model.RecipeNotFoundException
import com.household.domain.port.`in`.GetPlannedMealForDateUseCase
import com.household.domain.port.`in`.GetPlannedMealsForWeekUseCase
import com.household.domain.port.`in`.PlanMealCommand
import com.household.domain.port.`in`.PlanMealUseCase
import com.household.domain.port.`in`.UnplanMealUseCase
import com.household.domain.port.out.PlannedMealRepository
import com.household.domain.port.out.RecipeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class PlannedMealService(
    private val plannedMealRepository: PlannedMealRepository,
    private val recipeRepository: RecipeRepository,
) : PlanMealUseCase, UnplanMealUseCase, GetPlannedMealForDateUseCase, GetPlannedMealsForWeekUseCase {

    override fun plan(command: PlanMealCommand): PlannedMeal {
        require(command.recipeId != null || !command.title.isNullOrBlank()) {
            "Either recipeId or title must be provided"
        }

        val meal = if (command.recipeId != null) {
            val recipe = recipeRepository.findById(command.recipeId)
                ?: throw RecipeNotFoundException(command.recipeId)
            PlannedMeal.fromRecipe(command.householdId, command.date, recipe)
        } else {
            PlannedMeal.adHoc(command.householdId, command.date, command.title!!)
        }

        val existing = plannedMealRepository.findByHouseholdIdAndDate(command.householdId, command.date)
        if (existing != null) {
            plannedMealRepository.delete(existing.id)
        }

        return plannedMealRepository.save(meal)
    }

    override fun unplan(id: PlannedMealId) {
        plannedMealRepository.findById(id) ?: throw PlannedMealNotFoundException(id)
        plannedMealRepository.delete(id)
    }

    @Transactional(readOnly = true)
    override fun getForDate(householdId: HouseholdId, date: LocalDate): PlannedMeal? =
        plannedMealRepository.findByHouseholdIdAndDate(householdId, date)

    @Transactional(readOnly = true)
    override fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<PlannedMeal> =
        plannedMealRepository.findByHouseholdIdBetween(householdId, startDate, startDate.plusDays(6))
}

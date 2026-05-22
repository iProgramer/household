package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MealNotFoundException
import com.household.domain.port.`in`.AssignMealCommand
import com.household.domain.port.`in`.AssignMealUseCase
import com.household.domain.port.`in`.CreateMealCommand
import com.household.domain.port.`in`.CreateMealUseCase
import com.household.domain.port.`in`.DeleteMealUseCase
import com.household.domain.port.`in`.GetMealIdeasUseCase
import com.household.domain.port.`in`.GetMealsForDateUseCase
import com.household.domain.port.`in`.GetMealsForWeekUseCase
import com.household.domain.port.`in`.RenameMealUseCase
import com.household.domain.port.`in`.UnassignMealUseCase
import com.household.domain.port.out.MealRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class MealService(
    private val mealRepository: MealRepository,
) : CreateMealUseCase, GetMealIdeasUseCase, GetMealsForDateUseCase, GetMealsForWeekUseCase,
    AssignMealUseCase, UnassignMealUseCase, DeleteMealUseCase, RenameMealUseCase {

    override fun create(command: CreateMealCommand): Meal =
        mealRepository.save(Meal.createIdea(command.householdId, command.title))

    @Transactional(readOnly = true)
    override fun getIdeas(householdId: HouseholdId): List<Meal> =
        mealRepository.findIdeasByHouseholdId(householdId)

    @Transactional(readOnly = true)
    override fun getForDate(householdId: HouseholdId, date: LocalDate): List<Meal> =
        mealRepository.findPlannedByHouseholdIdAndDate(householdId, date)

    @Transactional(readOnly = true)
    override fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<Meal> =
        mealRepository.findPlannedByHouseholdIdBetween(householdId, startDate, startDate.plusDays(6))

    override fun assign(command: AssignMealCommand): Meal {
        val meal = mealRepository.findById(command.id) ?: throw MealNotFoundException(command.id)
        return mealRepository.save(meal.assign(command.date))
    }

    override fun unassign(id: MealId): Meal {
        val meal = mealRepository.findById(id) ?: throw MealNotFoundException(id)
        return mealRepository.save(meal.unassign())
    }

    override fun delete(id: MealId) = mealRepository.delete(id)

    override fun rename(id: MealId, title: String): Meal {
        val meal = mealRepository.findById(id) ?: throw MealNotFoundException(id)
        return mealRepository.save(meal.rename(title))
    }
}

package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.RecipeId
import com.household.domain.port.out.PlannedMealRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PlannedMealRepositoryAdapter(
    private val jpa: PlannedMealJpaRepository,
) : PlannedMealRepository {

    override fun save(meal: PlannedMeal): PlannedMeal =
        jpa.save(meal.toJpaEntity()).toDomain()

    override fun findById(id: PlannedMealId): PlannedMeal? =
        jpa.findById(id.value).orElse(null)?.toDomain()

    override fun findByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): PlannedMeal? =
        jpa.findByHouseholdIdAndDate(householdId.value, date)?.toDomain()

    override fun findByHouseholdIdBetween(householdId: HouseholdId, from: LocalDate, to: LocalDate): List<PlannedMeal> =
        jpa.findAllByHouseholdIdAndDateBetween(householdId.value, from, to).map { it.toDomain() }

    override fun delete(id: PlannedMealId) = jpa.deleteById(id.value)

    private fun PlannedMeal.toJpaEntity() = PlannedMealJpaEntity(
        id = id.value,
        householdId = householdId.value,
        date = date,
        title = title,
        recipeId = recipeId?.value,
    )

    private fun PlannedMealJpaEntity.toDomain() = PlannedMeal(
        id = PlannedMealId(id),
        householdId = HouseholdId(householdId),
        date = date,
        title = title,
        recipeId = recipeId?.let { RecipeId(it) },
    )
}

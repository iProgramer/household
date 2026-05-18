package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MealStatus
import com.household.domain.port.out.MealRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class MealRepositoryAdapter(
    private val jpa: MealJpaRepository,
) : MealRepository {

    override fun save(meal: Meal): Meal =
        jpa.save(meal.toJpaEntity()).toDomain()

    override fun findById(id: MealId): Meal? =
        jpa.findById(id.value).orElse(null)?.toDomain()

    override fun findIdeasByHouseholdId(householdId: HouseholdId): List<Meal> =
        jpa.findAllByHouseholdIdAndStatus(householdId.value, "IDEA").map { it.toDomain() }

    override fun findPlannedByHouseholdIdAndDate(householdId: HouseholdId, date: LocalDate): List<Meal> =
        jpa.findAllByHouseholdIdAndStatusAndDate(householdId.value, "PLANNED", date).map { it.toDomain() }

    override fun findPlannedByHouseholdIdBetween(householdId: HouseholdId, from: LocalDate, to: LocalDate): List<Meal> =
        jpa.findAllByHouseholdIdAndStatusAndDateBetween(householdId.value, "PLANNED", from, to).map { it.toDomain() }

    override fun delete(id: MealId) = jpa.deleteById(id.value)

    private fun Meal.toJpaEntity() = MealJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        date = date,
        status = status.name,
    )

    private fun MealJpaEntity.toDomain() = Meal(
        id = MealId(id),
        householdId = HouseholdId(householdId),
        title = title,
        date = date,
        status = MealStatus.valueOf(status),
    )
}

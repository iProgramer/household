package com.household.adapter.out.persistence

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.port.out.RecipeRepository
import org.springframework.stereotype.Component

@Component
class RecipeRepositoryAdapter(
    private val jpa: RecipeJpaRepository,
) : RecipeRepository {

    override fun save(recipe: Recipe): Recipe =
        jpa.save(recipe.toJpaEntity()).toDomain()

    override fun findById(id: RecipeId): Recipe? =
        jpa.findById(id.value).orElse(null)?.toDomain()

    override fun findAllByHouseholdId(householdId: HouseholdId): List<Recipe> =
        jpa.findAllByHouseholdId(householdId.value).map { it.toDomain() }

    override fun delete(id: RecipeId) = jpa.deleteById(id.value)

    private fun Recipe.toJpaEntity() = RecipeJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        favorite = favorite,
    )

    private fun RecipeJpaEntity.toDomain() = Recipe(
        id = RecipeId(id),
        householdId = HouseholdId(householdId),
        title = title,
        favorite = favorite,
    )
}

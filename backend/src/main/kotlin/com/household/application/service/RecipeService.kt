package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.model.RecipeNotFoundException
import com.household.domain.port.`in`.CreateRecipeCommand
import com.household.domain.port.`in`.CreateRecipeUseCase
import com.household.domain.port.`in`.DeleteRecipeUseCase
import com.household.domain.port.`in`.GetRecipesUseCase
import com.household.domain.port.`in`.RenameRecipeUseCase
import com.household.domain.port.`in`.ToggleFavoriteUseCase
import com.household.domain.port.out.RecipeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RecipeService(
    private val recipeRepository: RecipeRepository,
) : CreateRecipeUseCase, GetRecipesUseCase, RenameRecipeUseCase, DeleteRecipeUseCase, ToggleFavoriteUseCase {

    override fun create(command: CreateRecipeCommand): Recipe =
        recipeRepository.save(Recipe.create(command.householdId, command.title))

    @Transactional(readOnly = true)
    override fun getRecipes(householdId: HouseholdId): List<Recipe> =
        recipeRepository.findAllByHouseholdId(householdId)

    override fun rename(id: RecipeId, title: String): Recipe {
        val recipe = recipeRepository.findById(id) ?: throw RecipeNotFoundException(id)
        return recipeRepository.save(recipe.rename(title))
    }

    override fun delete(id: RecipeId) = recipeRepository.delete(id)

    override fun toggleFavorite(id: RecipeId): Recipe {
        val recipe = recipeRepository.findById(id) ?: throw RecipeNotFoundException(id)
        return recipeRepository.save(recipe.toggleFavorite())
    }
}

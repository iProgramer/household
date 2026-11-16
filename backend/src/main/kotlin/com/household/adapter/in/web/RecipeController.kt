package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.Recipe
import com.household.domain.model.RecipeId
import com.household.domain.port.`in`.CreateRecipeCommand
import com.household.domain.port.`in`.CreateRecipeUseCase
import com.household.domain.port.`in`.DeleteRecipeUseCase
import com.household.domain.port.`in`.GetRecipesUseCase
import com.household.domain.port.`in`.RenameRecipeUseCase
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/recipes")
class RecipeController(
    private val createRecipe: CreateRecipeUseCase,
    private val getRecipes: GetRecipesUseCase,
    private val renameRecipe: RenameRecipeUseCase,
    private val deleteRecipe: DeleteRecipeUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: CreateRecipeRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): RecipeResponse = RecipeResponse.from(createRecipe.create(CreateRecipeCommand(principal.householdId, request.title)))

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthenticatedMember): List<RecipeResponse> =
        getRecipes.getRecipes(principal.householdId).map(RecipeResponse::from)

    @PatchMapping("/{id}")
    fun rename(
        @PathVariable id: UUID,
        @RequestBody request: RenameRecipeRequest,
    ): RecipeResponse = RecipeResponse.from(renameRecipe.rename(RecipeId(id), request.title))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ) = deleteRecipe.delete(RecipeId(id))
}

data class CreateRecipeRequest(val title: String)
data class RenameRecipeRequest(val title: String)

data class RecipeResponse(
    val id: UUID,
    val title: String,
) {
    companion object {
        fun from(recipe: Recipe) = RecipeResponse(
            id = recipe.id.value,
            title = recipe.title,
        )
    }
}

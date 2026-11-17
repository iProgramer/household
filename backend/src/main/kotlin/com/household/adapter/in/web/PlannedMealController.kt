package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.RecipeId
import com.household.domain.port.`in`.GetPlannedMealForDateUseCase
import com.household.domain.port.`in`.GetPlannedMealsForWeekUseCase
import com.household.domain.port.`in`.GetRecipesUseCase
import com.household.domain.port.`in`.PlanMealCommand
import com.household.domain.port.`in`.PlanMealUseCase
import com.household.domain.port.`in`.TogglePlannedMealFavoriteUseCase
import com.household.domain.port.`in`.UnplanMealUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/planned-meals")
class PlannedMealController(
    private val planMeal: PlanMealUseCase,
    private val unplanMeal: UnplanMealUseCase,
    private val getForDate: GetPlannedMealForDateUseCase,
    private val getForWeek: GetPlannedMealsForWeekUseCase,
    private val togglePlannedMealFavorite: TogglePlannedMealFavoriteUseCase,
    private val getRecipes: GetRecipesUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun plan(
        @RequestBody request: PlanMealRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): PlannedMealResponse {
        val meal = planMeal.plan(
            PlanMealCommand(
                householdId = principal.householdId,
                date = request.date,
                recipeId = request.recipeId?.let { RecipeId(it) },
                title = request.title,
            ),
        )
        val favoriteMap = buildFavoriteMap(principal)
        return PlannedMealResponse.from(meal, favoriteMap[meal.recipeId?.value])
    }

    @GetMapping("/date/{date}")
    fun getForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): PlannedMealResponse? {
        val meal = getForDate.getForDate(principal.householdId, date) ?: return null
        val favoriteMap = buildFavoriteMap(principal)
        return PlannedMealResponse.from(meal, favoriteMap[meal.recipeId?.value])
    }

    @GetMapping("/week")
    fun getForWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): List<PlannedMealResponse> {
        val meals = getForWeek.getForWeek(principal.householdId, startDate)
        val favoriteMap = buildFavoriteMap(principal)
        return meals.map { PlannedMealResponse.from(it, favoriteMap[it.recipeId?.value]) }
    }

    @PostMapping("/{id}/toggle-favorite")
    fun toggleFavorite(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): TogglePlannedMealFavoriteResponse {
        val result = togglePlannedMealFavorite.toggleFavorite(PlannedMealId(id), principal.householdId)
        return TogglePlannedMealFavoriteResponse(recipeId = result.recipeId, favorite = result.favorite)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unplan(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ) = unplanMeal.unplan(PlannedMealId(id))

    private fun buildFavoriteMap(principal: AuthenticatedMember): Map<UUID, Boolean> =
        getRecipes.getRecipes(principal.householdId).associate { it.id.value to it.favorite }
}

data class PlanMealRequest(
    val date: LocalDate,
    val recipeId: UUID?,
    val title: String?,
)

data class PlannedMealResponse(
    val id: UUID,
    val date: String,
    val title: String,
    val recipeId: UUID?,
    val favorite: Boolean?,
) {
    companion object {
        fun from(meal: PlannedMeal, favorite: Boolean? = null) = PlannedMealResponse(
            id = meal.id.value,
            date = meal.date.toString(),
            title = meal.title,
            recipeId = meal.recipeId?.value,
            favorite = if (meal.recipeId != null) favorite else null,
        )
    }
}

data class TogglePlannedMealFavoriteResponse(
    val recipeId: UUID,
    val favorite: Boolean,
)

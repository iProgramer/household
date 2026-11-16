package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.PlannedMeal
import com.household.domain.model.PlannedMealId
import com.household.domain.model.RecipeId
import com.household.domain.port.`in`.GetPlannedMealForDateUseCase
import com.household.domain.port.`in`.GetPlannedMealsForWeekUseCase
import com.household.domain.port.`in`.PlanMealCommand
import com.household.domain.port.`in`.PlanMealUseCase
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
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun plan(
        @RequestBody request: PlanMealRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): PlannedMealResponse = PlannedMealResponse.from(
        planMeal.plan(
            PlanMealCommand(
                householdId = principal.householdId,
                date = request.date,
                recipeId = request.recipeId?.let { RecipeId(it) },
                title = request.title,
            ),
        ),
    )

    @GetMapping("/date/{date}")
    fun getForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): PlannedMealResponse? = getForDate.getForDate(principal.householdId, date)?.let(PlannedMealResponse::from)

    @GetMapping("/week")
    fun getForWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): List<PlannedMealResponse> = getForWeek.getForWeek(principal.householdId, startDate).map(PlannedMealResponse::from)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unplan(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ) = unplanMeal.unplan(PlannedMealId(id))
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
) {
    companion object {
        fun from(meal: PlannedMeal) = PlannedMealResponse(
            id = meal.id.value,
            date = meal.date.toString(),
            title = meal.title,
            recipeId = meal.recipeId?.value,
        )
    }
}

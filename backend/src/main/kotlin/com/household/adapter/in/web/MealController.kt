package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.Meal
import com.household.domain.model.MealId
import com.household.domain.model.MealStatus
import com.household.domain.port.`in`.AssignMealCommand
import com.household.domain.port.`in`.AssignMealUseCase
import com.household.domain.port.`in`.CreateMealCommand
import com.household.domain.port.`in`.CreateMealUseCase
import com.household.domain.port.`in`.DeleteMealUseCase
import com.household.domain.port.`in`.GetMealIdeasUseCase
import com.household.domain.port.`in`.GetMealsForDateUseCase
import com.household.domain.port.`in`.GetMealsForWeekUseCase
import com.household.domain.port.`in`.UnassignMealUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/meals")
class MealController(
    private val createMeal: CreateMealUseCase,
    private val getIdeas: GetMealIdeasUseCase,
    private val getForDate: GetMealsForDateUseCase,
    private val getForWeek: GetMealsForWeekUseCase,
    private val assignMeal: AssignMealUseCase,
    private val unassignMeal: UnassignMealUseCase,
    private val deleteMeal: DeleteMealUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: CreateMealRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): MealResponse = MealResponse.from(createMeal.create(CreateMealCommand(principal.householdId, request.title)))

    @GetMapping("/ideas")
    fun getIdeas(@AuthenticationPrincipal principal: AuthenticatedMember): List<MealResponse> =
        getIdeas.getIdeas(principal.householdId).map(MealResponse::from)

    @GetMapping("/date/{date}")
    fun getForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): List<MealResponse> = getForDate.getForDate(principal.householdId, date).map(MealResponse::from)

    @GetMapping("/week")
    fun getForWeek(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): List<MealResponse> = getForWeek.getForWeek(principal.householdId, startDate).map(MealResponse::from)

    @PutMapping("/{id}/assign")
    fun assign(
        @PathVariable id: UUID,
        @RequestBody request: AssignMealRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): MealResponse = MealResponse.from(assignMeal.assign(AssignMealCommand(MealId(id), request.date)))

    @PutMapping("/{id}/unassign")
    fun unassign(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): MealResponse = MealResponse.from(unassignMeal.unassign(MealId(id)))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ) = deleteMeal.delete(MealId(id))
}

data class CreateMealRequest(val title: String)
data class AssignMealRequest(val date: LocalDate)

data class MealResponse(
    val id: UUID,
    val title: String,
    val date: String?,
    val status: MealStatus,
) {
    companion object {
        fun from(meal: Meal) = MealResponse(
            id = meal.id.value,
            title = meal.title,
            date = meal.date?.toString(),
            status = meal.status,
        )
    }
}

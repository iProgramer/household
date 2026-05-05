package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.MealNote
import com.household.domain.port.`in`.GetMealNoteForDateUseCase
import com.household.domain.port.`in`.GetWeekMealNotesUseCase
import com.household.domain.port.`in`.SetMealNoteCommand
import com.household.domain.port.`in`.SetMealNoteUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/meal-notes")
class MealNoteController(
    private val setMealNote: SetMealNoteUseCase,
    private val getForDate: GetMealNoteForDateUseCase,
    private val getForWeek: GetWeekMealNotesUseCase,
) {
    @PutMapping("/{date}")
    fun set(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody request: MealNoteRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): MealNoteResponse = MealNoteResponse.from(
        setMealNote.set(SetMealNoteCommand(principal.householdId, date, request.note))
    )

    @GetMapping("/today")
    fun getToday(@AuthenticationPrincipal principal: AuthenticatedMember): ResponseEntity<MealNoteResponse> {
        val note = getForDate.getForDate(principal.householdId, LocalDate.now())
        return if (note != null) ResponseEntity.ok(MealNoteResponse.from(note))
        else ResponseEntity.noContent().build()
    }

    @GetMapping("/week")
    fun getWeek(
        @AuthenticationPrincipal principal: AuthenticatedMember,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
    ): List<MealNoteResponse> =
        getForWeek.getForWeek(principal.householdId, startDate).map(MealNoteResponse::from)
}

data class MealNoteRequest(val note: String)

data class MealNoteResponse(val date: LocalDate, val note: String) {
    companion object {
        fun from(mealNote: MealNote) = MealNoteResponse(mealNote.date, mealNote.note)
    }
}

package com.household.adapter.`in`.web

import com.household.adapter.`in`.web.security.AuthenticatedMember
import com.household.domain.model.FixedEvent
import com.household.domain.model.FixedEventId
import com.household.domain.model.RecurrenceRule
import com.household.domain.port.`in`.CreateFixedEventCommand
import com.household.domain.port.`in`.CreateFixedEventUseCase
import com.household.domain.port.`in`.DeleteFixedEventUseCase
import com.household.domain.port.`in`.GetFixedEventsForDateUseCase
import com.household.domain.port.`in`.GetFixedEventsForWeekUseCase
import com.household.domain.port.`in`.UpdateFixedEventCommand
import com.household.domain.port.`in`.UpdateFixedEventUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
@RequestMapping("/api/fixed-events")
class FixedEventController(
    private val createFixedEvent: CreateFixedEventUseCase,
    private val getForDate: GetFixedEventsForDateUseCase,
    private val getForWeek: GetFixedEventsForWeekUseCase,
    private val deleteFixedEvent: DeleteFixedEventUseCase,
    private val updateFixedEvent: UpdateFixedEventUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody @Valid request: CreateFixedEventRequest,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ): FixedEventResponse = FixedEventResponse.from(
        createFixedEvent.create(
            CreateFixedEventCommand(
                householdId = principal.householdId,
                title = request.title,
                date = request.date,
                recurrenceRule = request.recurrence.toDomain(),
            )
        )
    )

    @GetMapping("/today")
    fun getToday(@AuthenticationPrincipal principal: AuthenticatedMember): List<FixedEventResponse> =
        getForDate.getForDate(principal.householdId, LocalDate.now()).map(FixedEventResponse::from)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateFixedEventRequest,
    ): FixedEventResponse = FixedEventResponse.from(
        updateFixedEvent.update(
            UpdateFixedEventCommand(
                id = FixedEventId(id),
                title = request.title,
                date = request.date,
                recurrenceRule = request.recurrence.toDomain(),
            )
        )
    )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedMember,
    ) = deleteFixedEvent.delete(FixedEventId(id))

    @GetMapping("/week")
    fun getWeek(
        @AuthenticationPrincipal principal: AuthenticatedMember,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
    ): List<FixedEventResponse> =
        getForWeek.getForWeek(principal.householdId, startDate).map(FixedEventResponse::from)
}

data class CreateFixedEventRequest(
    @field:NotBlank val title: String,
    val date: LocalDate,
    @field:NotNull val recurrence: RecurrenceRequest,
)

data class UpdateFixedEventRequest(
    @field:NotBlank val title: String,
    val date: LocalDate,
    @field:NotNull val recurrence: RecurrenceRequest,
)

data class FixedEventResponse(
    val id: UUID,
    val title: String,
    val date: LocalDate,
    val recurrence: RecurrenceResponse?,
) {
    companion object {
        fun from(event: FixedEvent) = FixedEventResponse(
            id = event.id.value,
            title = event.title,
            date = event.date,
            recurrence = event.recurrenceRule?.toResponse(),
        )
    }
}

private fun RecurrenceRule.toResponse(): RecurrenceResponse = when (this) {
    is RecurrenceRule.Daily -> RecurrenceResponse("DAILY", null)
    is RecurrenceRule.Weekly -> RecurrenceResponse("WEEKLY", null)
    is RecurrenceRule.Biweekly -> RecurrenceResponse("BIWEEKLY", null)
    is RecurrenceRule.Monthly -> RecurrenceResponse("MONTHLY", null)
    is RecurrenceRule.OnWeekday -> RecurrenceResponse("ON_WEEKDAY", dayOfWeek.name)
}

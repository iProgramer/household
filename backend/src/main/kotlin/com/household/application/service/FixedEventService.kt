package com.household.application.service

import com.household.domain.model.FixedEvent
import com.household.domain.model.FixedEventId
import com.household.domain.model.FixedEventNotFoundException
import com.household.domain.model.HouseholdId
import com.household.domain.port.`in`.CreateFixedEventCommand
import com.household.domain.port.`in`.CreateFixedEventUseCase
import com.household.domain.port.`in`.DeleteFixedEventUseCase
import com.household.domain.port.`in`.GetFixedEventsForDateUseCase
import com.household.domain.port.`in`.GetFixedEventsForWeekUseCase
import com.household.domain.port.`in`.UpdateFixedEventCommand
import com.household.domain.port.`in`.UpdateFixedEventUseCase
import com.household.domain.port.out.FixedEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class FixedEventService(
    private val fixedEventRepository: FixedEventRepository,
) : CreateFixedEventUseCase, GetFixedEventsForDateUseCase, GetFixedEventsForWeekUseCase,
    DeleteFixedEventUseCase, UpdateFixedEventUseCase {

    override fun create(command: CreateFixedEventCommand): FixedEvent =
        fixedEventRepository.save(
            FixedEvent.create(command.householdId, command.title, command.date, command.recurrenceRule)
        )

    @Transactional(readOnly = true)
    override fun getForDate(householdId: HouseholdId, date: LocalDate): List<FixedEvent> =
        fixedEventRepository.findAllByHouseholdId(householdId).filter { it.occursOn(date) }

    override fun delete(id: FixedEventId) = fixedEventRepository.delete(id)

    override fun update(command: UpdateFixedEventCommand): FixedEvent {
        val event = fixedEventRepository.findById(command.id) ?: throw FixedEventNotFoundException(command.id)
        return fixedEventRepository.save(event.update(command.title, command.date, command.recurrenceRule))
    }

    @Transactional(readOnly = true)
    override fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<FixedEvent> {
        val days = (0L..6L).map { startDate.plusDays(it) }
        return fixedEventRepository.findAllByHouseholdId(householdId)
            .flatMap { event -> days.filter { event.occursOn(it) }.map { day -> event.copy(date = day) } }
    }
}

package com.household.application.service

import com.household.domain.model.HouseholdId
import com.household.domain.model.MealNote
import com.household.domain.port.`in`.GetMealNoteForDateUseCase
import com.household.domain.port.`in`.GetWeekMealNotesUseCase
import com.household.domain.port.`in`.SetMealNoteCommand
import com.household.domain.port.`in`.SetMealNoteUseCase
import com.household.domain.port.out.MealNoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class MealNoteService(
    private val mealNoteRepository: MealNoteRepository,
) : SetMealNoteUseCase, GetMealNoteForDateUseCase, GetWeekMealNotesUseCase {

    override fun set(command: SetMealNoteCommand): MealNote =
        mealNoteRepository.save(MealNote(command.householdId, command.date, command.note))

    @Transactional(readOnly = true)
    override fun getForDate(householdId: HouseholdId, date: LocalDate): MealNote? =
        mealNoteRepository.findByHouseholdIdAndDate(householdId, date)

    @Transactional(readOnly = true)
    override fun getForWeek(householdId: HouseholdId, startDate: LocalDate): List<MealNote> =
        mealNoteRepository.findAllByHouseholdIdAndDateBetween(householdId, startDate, startDate.plusDays(6))
}

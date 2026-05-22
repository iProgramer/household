package com.household.adapter.out.persistence

import com.household.domain.model.FixedEvent
import com.household.domain.model.FixedEventId
import com.household.domain.model.HouseholdId
import com.household.domain.model.RecurrenceRule
import com.household.domain.port.out.FixedEventRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class FixedEventRepositoryAdapter(
    private val jpa: FixedEventJpaRepository,
) : FixedEventRepository {

    override fun save(event: FixedEvent): FixedEvent =
        jpa.save(event.toJpaEntity()).toDomain()

    override fun findAllByHouseholdId(householdId: HouseholdId): List<FixedEvent> =
        jpa.findAllByHouseholdId(householdId.value).map { it.toDomain() }

    override fun findById(id: FixedEventId): FixedEvent? =
        jpa.findByIdOrNull(id.value)?.toDomain()

    override fun delete(id: FixedEventId) = jpa.deleteById(id.value)

    private fun FixedEvent.toJpaEntity() = FixedEventJpaEntity(
        id = id.value,
        householdId = householdId.value,
        title = title,
        date = date,
        recurrenceType = recurrenceRule?.typeName(),
        recurrenceWeekday = (recurrenceRule as? RecurrenceRule.OnWeekday)?.dayOfWeek?.name,
    )

    private fun FixedEventJpaEntity.toDomain() = FixedEvent(
        id = FixedEventId(id),
        householdId = HouseholdId(householdId),
        title = title,
        date = date,
        recurrenceRule = toRecurrenceRule(recurrenceType, recurrenceWeekday),
    )

    private fun RecurrenceRule.typeName(): String = when (this) {
        is RecurrenceRule.Daily -> "DAILY"
        is RecurrenceRule.Weekly -> "WEEKLY"
        is RecurrenceRule.Biweekly -> "BIWEEKLY"
        is RecurrenceRule.Monthly -> "MONTHLY"
        is RecurrenceRule.OnWeekday -> "ON_WEEKDAY"
    }

    private fun toRecurrenceRule(type: String?, weekday: String?): RecurrenceRule? = when (type) {
        null -> null
        "DAILY" -> RecurrenceRule.Daily
        "WEEKLY" -> RecurrenceRule.Weekly
        "BIWEEKLY" -> RecurrenceRule.Biweekly
        "MONTHLY" -> RecurrenceRule.Monthly
        "ON_WEEKDAY" -> RecurrenceRule.OnWeekday(DayOfWeek.valueOf(requireNotNull(weekday)))
        else -> null
    }
}

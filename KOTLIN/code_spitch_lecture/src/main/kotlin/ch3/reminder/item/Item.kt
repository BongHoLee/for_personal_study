package ch3.reminder.item

import ch3.reminder.scheduler.Scheduler
import kotlinx.datetime.Instant

class Item(var title: String, var content: String) {
    private val schedules = hashSetOf<Scheduler>()

    fun addSchedule(vararg scheduler: Scheduler) {
        schedules += scheduler
    }

    fun send(now: Instant) {
        schedules.forEach {it.send(this, now)}
    }

}

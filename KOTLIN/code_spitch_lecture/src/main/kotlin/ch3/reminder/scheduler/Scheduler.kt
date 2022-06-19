package ch3.reminder.scheduler

import ch3.reminder.item.Item
import ch3.reminder.sender.Sender
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

abstract class Scheduler {
    private val senders = hashSetOf<Sender>()

    fun addSenders(vararg sender: Sender) {
        this.senders += sender
    }

    fun send(item: Item, now: Instant) {
        if (!isSend(now)) {
            senders.forEach {it.send(item)}
        }
    }

    protected abstract fun isSend(now: Instant): Boolean
}

class Once(private val at: Instant): Scheduler() {
    private var isSent = false

    override fun isSend(now: Instant): Boolean {
        return if (!isSent && at <= now) {
            isSent = true
            false
        } else {
            true
        }
    }
}

class RepeatDay(private val hour: Int, private val minute: Int, private vararg val days: DayOfWeek): Scheduler() {
    private val isSent = hashMapOf<String, Boolean>()

    override fun isSend(now: Instant): Boolean {
        val dateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val nowDay = dateTime.dayOfWeek
        val nowHour = dateTime.hour
        val nowMinute = dateTime.minute
        val key = "${dateTime.dayOfYear} ${dateTime.month} ${dateTime.date} $nowDay $nowHour:$nowMinute"

        if (isSent[key] == true || nowDay !in days || nowHour > hour) {
            return true
        }

        return if (nowHour == hour && nowMinute > minute) {
            isSent[key] = true
            false
        } else {
            true
        }
    }

}
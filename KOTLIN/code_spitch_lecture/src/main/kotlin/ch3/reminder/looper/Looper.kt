package ch3.reminder.looper

import ch3.reminder.user.User
import kotlinx.datetime.Clock

class Looper(
    private val started: (Looper) -> Unit,
    private val ended: (Looper) -> Unit
) {
    companion object {
        val users = hashSetOf<User>()
    }

    var isRunning = false
        private set

    fun addUser(user: User) = users.add(user)

    fun start() {
        isRunning = true
        started(this)
    }

    fun end() {
        isRunning = false
        ended(this)
    }
}

fun main() {
    val started: (Looper) -> Unit = {

        val thread = Thread {
            while(it.isRunning && !Thread.currentThread().isInterrupted) {
                val now = Clock.System.now()
                Looper.users.forEach { it.send(now) }
                Thread.sleep(1000)
            }
        }
        if (!thread.isAlive) thread.start()

    }

    val ended: (Looper) -> Unit = {}

    val looper = Looper(started, ended)
}
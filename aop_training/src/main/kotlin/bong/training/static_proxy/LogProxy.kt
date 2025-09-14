package bong.training.static_proxy

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class LogProxy {
    private val log = LoggerFactory.getLogger(LogProxy::class.java)
    private val callCount = AtomicInteger(0)

    fun <R>  executeWithLog(target: () -> R) : R {
        callCount.incrementAndGet()
        println("Log start")
        val result = target()
        println("Log end")

        return result
    }

    fun getCallCount(): Int = callCount.get()

    fun resetCallCount() {
        callCount.set(0)
    }
}
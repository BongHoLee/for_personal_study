package com.codex.consumer.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "codex.kafka")
class KafkaConsumerProperties {

    val listener: Listener = Listener()
    val retry: Retry = Retry()
    val avro: Avro = Avro()

    class Listener {
        var concurrency: Int = 1
        var ackMode: String = "MANUAL_IMMEDIATE"
    }

    class Retry {
        var intervalMillis: Long = 1_000
        var maxAttempts: Long = 2
    }

    class Avro {
        var specificReader: Boolean = true
    }
}

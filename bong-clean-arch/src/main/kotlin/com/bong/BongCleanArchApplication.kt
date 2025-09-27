package com.bong

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BongCleanArchApplication

fun main(args: Array<String>) {
    runApplication<BongCleanArchApplication>(*args)
}

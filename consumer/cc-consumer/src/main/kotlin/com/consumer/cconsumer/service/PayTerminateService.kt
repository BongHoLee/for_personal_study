package com.consumer.cconsumer.service

interface PayTerminateService {
    fun processTermination(payAccountId: Long, reason: String)
}
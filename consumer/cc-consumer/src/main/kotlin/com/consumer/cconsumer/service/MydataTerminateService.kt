package com.consumer.cconsumer.service

interface MydataTerminateService {
    fun processTermination(payAccountId: Long, reason: String)
}
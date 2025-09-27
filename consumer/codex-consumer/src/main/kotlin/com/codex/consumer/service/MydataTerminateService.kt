package com.codex.consumer.service

interface MydataTerminateService {
    fun handleTermination(payAccountId: Long, reason: String?)
}

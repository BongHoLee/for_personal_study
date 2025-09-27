package com.codex.consumer.service

interface PayTerminateService {
    fun handleTermination(payAccountId: Long, reason: String?)
}

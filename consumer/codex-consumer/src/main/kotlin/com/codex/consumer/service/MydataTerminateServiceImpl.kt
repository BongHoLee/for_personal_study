package com.codex.consumer.service

import com.codex.consumer.domain.entity.MydataTerminateUser
import com.codex.consumer.domain.entity.TerminateStatus
import com.codex.consumer.domain.repository.MydataTerminateUserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MydataTerminateServiceImpl(
    private val repository: MydataTerminateUserRepository
) : MydataTerminateService {

    @Transactional
    override fun handleTermination(payAccountId: Long, reason: String?) {
        val existing = repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
        if (existing != null) {
            if (reason != null && reason != existing.reason) {
                existing.reason = reason
                log.debug("Updated existing PENDING record for payAccountId={} with new reason", payAccountId)
            } else {
                log.debug("Skip creating duplicate PENDING record for payAccountId={}", payAccountId)
            }
            return
        }

        val newRecord = MydataTerminateUser(
            payAccountId = payAccountId,
            terminateStatus = TerminateStatus.PENDING,
            reason = reason
        )

        try {
            repository.save(newRecord)
            log.info("Registered mydata termination target. payAccountId={}, reason={} ", payAccountId, reason)
        } catch (ex: DataIntegrityViolationException) {
            log.warn("Detected concurrent insert for payAccountId={}, falling back to existing record", payAccountId, ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MydataTerminateServiceImpl::class.java)
    }
}

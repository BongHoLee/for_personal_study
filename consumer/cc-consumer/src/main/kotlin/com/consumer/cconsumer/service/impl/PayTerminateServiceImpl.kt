package com.consumer.cconsumer.service.impl

import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import com.consumer.cconsumer.domain.repository.PayTerminateUserRepository
import com.consumer.cconsumer.service.PayTerminateService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PayTerminateServiceImpl(
    private val repository: PayTerminateUserRepository
) : PayTerminateService {
    
    private val logger = LoggerFactory.getLogger(PayTerminateServiceImpl::class.java)

    @Transactional
    override fun processTermination(payAccountId: Long, reason: String) {
        logger.info("Processing pay account termination for payAccountId: {}, reason: {}", payAccountId, reason)
        
        try {
            // 멱등성 보장: PENDING 상태 레코드 존재 여부 확인
            val existingPending = repository.findByPayAccountIdAndTerminateStatus(
                payAccountId, TerminateStatus.PENDING
            )
            
            if (existingPending != null) {
                logger.info("PENDING status record already exists for payAccountId: {}. Skipping insertion.", payAccountId)
                return
            }
            
            // PENDING 상태 레코드가 없으면 새로 생성
            val newTerminateUser = PayTerminateUser(
                payAccountId = payAccountId,
                terminateStatus = TerminateStatus.PENDING,
                reason = reason
            )
            
            repository.save(newTerminateUser)
            logger.info("Successfully created PENDING termination record for payAccountId: {}", payAccountId)
            
        } catch (exception: DataIntegrityViolationException) {
            // 복합 유니크 제약조건 위반 시 (동시성 상황에서 발생 가능)
            logger.warn("Unique constraint violation for payAccountId: {} with PENDING status. Another transaction may have inserted the record.", payAccountId)
            // 멱등성 보장을 위해 예외를 삼키고 정상 처리로 간주
            
        } catch (exception: Exception) {
            logger.error("Failed to process pay account termination for payAccountId: {}, reason: {}", 
                payAccountId, reason, exception)
            throw exception
        }
    }
}
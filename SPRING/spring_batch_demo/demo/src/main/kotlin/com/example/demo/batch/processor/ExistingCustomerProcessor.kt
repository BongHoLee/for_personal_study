package com.example.demo.batch.processor

import com.example.demo.domain.primary.Customer
import com.example.demo.dto.CustomerCsvRow
import com.example.demo.dto.CustomerType
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

/**
 * EXISTING 타입 고객 전용 Processor
 *
 * - EXISTING 타입만 처리, 나머지는 필터링 (null 반환)
 * - 18세 미만 필터링
 * - 이름 대문자, 이메일 소문자 변환
 */
@Component
class ExistingCustomerProcessor : ItemProcessor<CustomerCsvRow, Customer> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(item: CustomerCsvRow): Customer? {
        // 1. EXISTING 타입만 처리
        if (item.type.uppercase() != CustomerType.EXISTING.name) {
            return null  // 다른 타입은 이 Step에서 처리하지 않음
        }

        // 2. 나이 검증
        val age = item.age.toIntOrNull()
        if (age == null) {
            logger.warn("유효하지 않은 나이: ${item.email}")
            return null
        }

        // 3. 18세 미만 필터링
        if (age < 18) {
            logger.info("미성년자 필터링: ${item.firstName} ${item.lastName} (나이: $age)")
            return null
        }

        // 4. 데이터 변환 및 엔티티 생성
        val firstName = item.firstName.trim().uppercase()
        val lastName = item.lastName.trim().uppercase()
        val email = item.email.trim().lowercase()

        logger.info("[EXISTING] 고객 처리: $firstName $lastName -> Primary DB")

        return Customer(
            firstName = firstName,
            lastName = lastName,
            email = email,
            age = age,
            isActive = true
        )
    }
}

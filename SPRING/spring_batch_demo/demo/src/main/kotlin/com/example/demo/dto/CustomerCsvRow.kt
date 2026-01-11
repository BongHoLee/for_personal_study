package com.example.demo.dto

/**
 * CSV 파일에서 읽어온 원본 데이터를 담는 DTO
 *
 * Spring Batch의 ItemReader가 CSV 파일을 읽을 때 이 클래스로 매핑됩니다.
 *
 * type 필드에 따라 다른 DB에 저장:
 * - EXISTING: Primary DB (customers 테이블)
 * - NEW: Secondary DB (new_customers 테이블)
 *
 * 주의: BeanWrapperFieldSetMapper는 기본 생성자와 setter를 사용하므로
 * var 프로퍼티와 기본값이 필요합니다.
 */
class CustomerCsvRow(
    var type: String = "",      // EXISTING 또는 NEW
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var age: String = ""
)

/**
 * 고객 타입 열거형
 */
enum class CustomerType {
    EXISTING,  // Primary DB에 저장
    NEW        // Secondary DB에 저장
}

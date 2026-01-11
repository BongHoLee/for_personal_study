package com.example.demo.domain.secondary

import jakarta.persistence.*

/**
 * NewCustomer 엔티티 (Secondary DB)
 *
 * CSV에서 type='NEW'인 고객 데이터가 저장됩니다.
 * 물리적으로 분리된 별도 MySQL DB에 저장됩니다.
 */
@Entity
@Table(name = "new_customers")
class NewCustomer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val age: Int,

    @Column(nullable = false)
    val isActive: Boolean = true
)

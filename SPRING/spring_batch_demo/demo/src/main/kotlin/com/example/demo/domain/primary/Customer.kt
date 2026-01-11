package com.example.demo.domain.primary

import jakarta.persistence.*

/**
 * Customer 엔티티 (Primary DB)
 *
 * CSV에서 type='EXISTING'인 고객 데이터가 저장됩니다.
 */
@Entity
@Table(name = "customers")
class Customer(
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

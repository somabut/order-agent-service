package com.orderagentservice.order.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "kiosk")
class KioskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kiosk_id")
    val kioskId: Long? = null,

    @Column(name = "name", length = 100)
    val name: String? = null,

    @Column(name = "screen_weight", nullable = false)
    val screenWeight: Int = 1920,

    @Column(name = "screen_hegiht", nullable = false)
    val screenHeight: Int = 1080
)
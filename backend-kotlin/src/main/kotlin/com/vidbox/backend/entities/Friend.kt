package com.vidbox.backend.entities

import javax.persistence.*

@Entity
@Table(name = "friends")
data class Friend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "frienda", nullable = false)
    var friendAId: Int? = null,

    @Column(name = "friendb", nullable = false)
    var friendBId: Int? = null,
)

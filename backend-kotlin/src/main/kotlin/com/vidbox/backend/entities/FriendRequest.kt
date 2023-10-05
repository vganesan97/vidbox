package com.vidbox.backend.entities

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "friend_requests")
data class FriendRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "requester", nullable = false)
    var requester: Int? = null,

    @Column(name = "requested", nullable = false)
    var requested: Int? = null,

    @Column(name = "status", nullable = false)
    var status: Int? = null,

)

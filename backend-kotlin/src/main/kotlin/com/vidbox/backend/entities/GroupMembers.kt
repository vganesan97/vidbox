package com.vidbox.backend.entities

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "group_members")
data class GroupMembers(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "group_id", nullable = false)
    var groupId: Int? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Int? = null,
)

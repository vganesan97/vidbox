package com.vidbox.backend.entities

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "group_infos")
data class GroupInfos(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "group_name", nullable = false)
    var groupName: String? = null,

    @Column(name = "group_description", nullable = true)
    var groupDescription: String? = null,

    @Column(name = "group_admin_id", nullable = false)
    var groupAdminId: Int? = null,

    @Column(name = "privacy", nullable = false)
    var privacy: String? = null,

    @Column(name = "group_avatar", nullable = true)
    var groupAvatar: String? = null
)

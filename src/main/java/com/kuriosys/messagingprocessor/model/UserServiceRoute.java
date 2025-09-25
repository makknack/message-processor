package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.UserServiceRouteStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_service_route")
@Data
public class UserServiceRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "service_route_id", length = 26, nullable = false)
    private String serviceRouteId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private UserServiceRouteStatus status = UserServiceRouteStatus.ACTIVE;

    @Column(name = "last_modified_by", nullable = false)
    private Long lastModifiedBy;

    @Column(name = "last_modified_at", nullable = false)
    private OffsetDateTime lastModifiedAt;
}

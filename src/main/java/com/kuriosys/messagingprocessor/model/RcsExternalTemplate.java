package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.RcsMessageTemplateStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "rcs_external_template")
public class RcsExternalTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_template_id", length = 26)
    private String messageTemplateId;

    @Column(name = "service_route_id", length = 26, nullable = false)
    private String serviceRouteId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private RcsMessageTemplateStatus status;

    @Column(name = "external_template_id", length = 36, nullable = false)
    private String externalTemplateId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}


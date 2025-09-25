package com.kuriosys.messagingprocessor.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "rcs_invalid_recipient")
@Data
public class RcsInvalidRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message_request_id", length = 26, nullable = false)
    private String messageRequestId;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

}


package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.RcsMessageRecipientStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "rcs_message_recipient")
@Data
public class RcsMessageRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "message_request_id", length = 26, nullable = false)
    private String messageRequestId;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private RcsMessageRecipientStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personalized_content", columnDefinition = "json")
    private Object personalizedContent;

    @Column(name = "request", columnDefinition = "text")
    private String request;

    @Column(name = "response", columnDefinition = "text")
    private String response;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "comments", columnDefinition = "json")
    private List<String> comments;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "reference_id", length = 60)
    private String referenceId;
}


package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "rcs_response_event")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RcsResponseEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "message_request_id", length = 26, nullable = false)
    private String messageRequestId;

    @Column(name = "agent_id", length = 36, nullable = false)
    private String agentId;

    @Column(name = "reference_id", length = 60)
    private String referenceId;

    @Column(name = "event_id", unique = true, nullable = false, length = 36)
    private String eventId;

    @Column(name = "sender_phone_number", length = 20)
    private String senderPhoneNumber;

    @Column(name = "user_phone_number", length = 20)
    private String userPhoneNumber;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details", columnDefinition = "json")
    private String errorDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json")
    private JsonNode payload;

    @Column(name = "source_event_time")
    private OffsetDateTime sourceEventTime;

    @Column(name = "event_received_at", nullable = false)
    private OffsetDateTime eventReceivedAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

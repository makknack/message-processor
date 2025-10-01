package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.kuriosys.messagingprocessor.enums.RcsEventLogStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "rcs_event_log")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RcsEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 36)
    private String eventId;

    @Column(name = "source", length = 20, nullable = false)
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json", nullable = false)
    private String payload;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", length = 20, nullable = false)
    private RcsEventLogStatus status = RcsEventLogStatus.NO_ACTION_NEEDED;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

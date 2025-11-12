package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.RcsContentType;
import com.kuriosys.messagingprocessor.enums.RcsMessageRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "rcs_message_request")
@Data
public class RcsMessageRequest {

    @Id
    @Column(name = "message_request_id", nullable = false, unique = true)
    private String messageRequestId;
    @Column(name = "user_id", nullable = false)
    private BigInteger userId;
    @Column(name = "message_template_id", nullable = false)
    private String messageTemplateId;
    @Column(name = "rcs_agent_id", nullable = false)
    private String rcsAgentId;
    @Column(name = "is_personalized", nullable = false)
    private Boolean isPersonalized;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private RcsMessageRequestStatus status;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "content_type", nullable = false)
    private RcsContentType contentType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "json")
    private String content;   // Store raw JSON string

    @Column(name = "comments")
    private String comments;

    @Column(name = "total_recipients", nullable = false)
    private Integer totalRecipients;

    @Column(name = "processed_record", nullable = false)
    private Integer processedRecord;

    @Column(name = "valid_recipients")
    private Integer validRecipients;

    @Column(name = "invalid_recipients")
    private Integer invalidRecipients;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "contact_group_id", length = 36)
    private String contactGroupId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

}


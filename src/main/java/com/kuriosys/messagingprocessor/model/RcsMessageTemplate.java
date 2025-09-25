package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.RcsContentType;
import com.kuriosys.messagingprocessor.enums.RcsMessageTemplateStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rcs_message_template")
@Getter
@Setter
@ToString
public class RcsMessageTemplate {
    @Id
    @Column(name = "message_template_id", length = 26)
    private String messageTemplateId;

    @Column(name = "agent_id", length = 36, nullable = false)
    private String agentId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "template_name", length = 50, nullable = false)
    private String templateName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "template_status", nullable = false)
    private RcsMessageTemplateStatus templateStatus = RcsMessageTemplateStatus.DRAFT;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "content_type", nullable = false)
    private RcsContentType contentType;

    @Column(name = "is_personalized", nullable = false)
    private Boolean isPersonalized;

    @Column(name = "has_variables", nullable = false)
    private Boolean hasVariables;

    @Column(name = "template_content", columnDefinition = "json", nullable = false)
    private Object templateContent;

    @Column(name = "template_variables", columnDefinition = "json")
    private String templateVariables;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}


package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.ContactGroupStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "contact_group")
public class ContactGroup {
    @Id
    @Column(name = "contact_group_id", length = 36)
    private String contactGroupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_name", length = 50, nullable = false)
    private String groupName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private ContactGroupStatus status = ContactGroupStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}


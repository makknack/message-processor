package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.ContactStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @Column(name = "contact_id", length = 26)
    private String contactId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mobile_number", length = 20, nullable = false)
    private String mobileNumber;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private ContactStatus status = ContactStatus.ACTIVE;

    @Column(name = "contact_group_id", length = 36)
    private String contactGroupId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}


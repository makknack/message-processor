package com.kuriosys.messagingprocessor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import com.kuriosys.messagingprocessor.enums.RcsExternalAgentStatus;

@Entity
@Table(name = "rcs_external_agent")
@Data
public class RcsExternalAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private BigInteger id;

    @Column(name = "agent_id", length = 36, nullable = false)
    private String agentId;

    @Column(name = "external_agent_id", length = 36, nullable = false)
    private String externalAgentId;

    @Column(name = "service_route_id", length = 26, nullable = false)
    private String serviceRouteId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "external_agent_status", nullable = false)
    private RcsExternalAgentStatus externalAgentStatus;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}


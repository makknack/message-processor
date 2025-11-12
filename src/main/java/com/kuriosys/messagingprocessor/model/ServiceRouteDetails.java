package com.kuriosys.messagingprocessor.model;

import com.kuriosys.messagingprocessor.enums.AuthType;
import com.kuriosys.messagingprocessor.enums.HttpMethod;
import com.kuriosys.messagingprocessor.enums.ServiceRouteStatus;
import com.kuriosys.messagingprocessor.enums.ServiceRouteType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "service_route_details")
@Data
public class ServiceRouteDetails {
    @Id
    @Column(name = "service_route_id", length = 26, nullable = false)
    private String serviceRouteId;

    @Column(name = "service_route_name", length = 100, nullable = false, unique = true)
    private String serviceRouteName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_route_type", nullable = false)
    private ServiceRouteType serviceRouteType;

    @Column(name = "service_provider", length = 32, nullable = false)
    private String serviceProvider;

    @Column(name = "base_url", length = 255, nullable = false)
    private String baseUrl;

    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false)
    private HttpMethod httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "auth_token", length = 512)
    private String authToken;

    @Column(name = "api_key", length = 512)
    private String apiKey;

    @Column(name = "request_batch_size", nullable = false)
    private int requestBatchSize;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private ServiceRouteStatus status ;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}


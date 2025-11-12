package com.kuriosys.messagingprocessor.service;

import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import com.kuriosys.messagingprocessor.enums.RcsContentType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@Builder
public class PayloadRequest {
    private final List<String> recipients;
    private final boolean personalized;
    private final Map<String, String> personalizedValues;
    private final BigInteger userId;
    private final String messageRequestId;
    private final String messageTemplateId;
    private final Object content;
    private final Object personalizedContent;
    private final RcsContentType contentType;
    private final String rcsExternalAgentId;
    private final String externalTemplateId;
    private final ServiceRouteDetails serviceRouteDetails;
}


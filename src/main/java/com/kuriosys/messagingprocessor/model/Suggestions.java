package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Suggestions {
    @JsonAlias("actionType")
    private String actionType; // Replace with ActionType enum if available

    @JsonAlias("displayText")
    private String displayText;

    @JsonAlias("postbackData")
    private String postbackData;

    @JsonAlias("actionUrl")
    private String actionUrl;

    @JsonAlias("phoneNumber")
    private String phoneNumber;

    @JsonAlias("locationLabel")
    private String locationLabel;

    @JsonAlias("locationLatitude")
    private Double locationLatitude;

    @JsonAlias("locationLongitude")
    private Double locationLongitude;

    @JsonAlias("fallbackUrl")
    private String fallbackUrl;

    @JsonAlias("eventTitle")
    private String eventTitle;

    @JsonAlias("eventDescription")
    private String eventDescription;

    @JsonAlias("eventStartTime")
    private OffsetDateTime eventStartTime;

    @JsonAlias("eventEndTime")
    private OffsetDateTime eventEndTime;
}


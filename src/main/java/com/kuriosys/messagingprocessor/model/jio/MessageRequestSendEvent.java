package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageRequestSendEvent {
    private String error;
    private List<String> inValidNumbers;
    @JsonProperty("inValidNumbers_Count")
    private int inValidNumbersCount;
    private String messageId;
    private List<String> reachableUsers;
    @JsonProperty("reachableUsers_Count")
    private int reachableUsersCount;
    private String referenceID;
    private List<String> repeatedNumbers;
    @JsonProperty("repeatedNumbers_Count")
    private int repeatedNumbersCount;
    private String status;
    @JsonProperty("total limit")
    private int totalLimit;
    private List<String> unReachableUsers;
    @JsonProperty("unReachableUsers_Count")
    private int unReachableUsersCount;
}


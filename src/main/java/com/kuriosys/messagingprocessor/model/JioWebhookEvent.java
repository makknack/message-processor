package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JioWebhookEvent {
    private String source;
    private Payload payload;
    private OffsetDateTime sentAt;

    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Payload getPayload() { return payload; }
    public void setPayload(Payload payload) { this.payload = payload; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private String error;
        private List<String> inValidNumbers;
        @JsonProperty("inValidNumbers_Count")
        private Integer inValidNumbersCount;
        private String messageId;
        private List<String> reachableUsers;
        @JsonProperty("reachableUsers_Count")
        private Integer reachableUsersCount;
        private String referenceID;
        private List<String> repeatedNumbers;
        @JsonProperty("repeatedNumbers_Count")
        private Integer repeatedNumbersCount;
        private String status;
        @JsonProperty("total limit")
        private Integer totalLimit;
        private List<String> unReachableUsers;
        @JsonProperty("unReachableUsers_Count")
        private Integer unReachableUsersCount;

        // Getters and setters for all fields
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public List<String> getInValidNumbers() { return inValidNumbers; }
        public void setInValidNumbers(List<String> inValidNumbers) { this.inValidNumbers = inValidNumbers; }
        public Integer getInValidNumbersCount() { return inValidNumbersCount; }
        public void setInValidNumbersCount(Integer inValidNumbersCount) { this.inValidNumbersCount = inValidNumbersCount; }
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public List<String> getReachableUsers() { return reachableUsers; }
        public void setReachableUsers(List<String> reachableUsers) { this.reachableUsers = reachableUsers; }
        public Integer getReachableUsersCount() { return reachableUsersCount; }
        public void setReachableUsersCount(Integer reachableUsersCount) { this.reachableUsersCount = reachableUsersCount; }
        public String getReferenceID() { return referenceID; }
        public void setReferenceID(String referenceID) { this.referenceID = referenceID; }
        public List<String> getRepeatedNumbers() { return repeatedNumbers; }
        public void setRepeatedNumbers(List<String> repeatedNumbers) { this.repeatedNumbers = repeatedNumbers; }
        public Integer getRepeatedNumbersCount() { return repeatedNumbersCount; }
        public void setRepeatedNumbersCount(Integer repeatedNumbersCount) { this.repeatedNumbersCount = repeatedNumbersCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getTotalLimit() { return totalLimit; }
        public void setTotalLimit(Integer totalLimit) { this.totalLimit = totalLimit; }
        public List<String> getUnReachableUsers() { return unReachableUsers; }
        public void setUnReachableUsers(List<String> unReachableUsers) { this.unReachableUsers = unReachableUsers; }
        public Integer getUnReachableUsersCount() { return unReachableUsersCount; }
        public void setUnReachableUsersCount(Integer unReachableUsersCount) { this.unReachableUsersCount = unReachableUsersCount; }
    }
}


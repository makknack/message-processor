package com.kuriosys.messagingprocessor.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class RcsSubmissionEvent extends Event {
    private String messageRequestId;
    private List<String> recipients;
    private List<Map<String, String>> templateVariables;
}

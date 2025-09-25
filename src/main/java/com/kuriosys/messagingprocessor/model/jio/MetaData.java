package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaData {
    private String orgMsgId;
    private OffsetDateTime orgMsgSendTime;
}


package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Error {
    private String code;
    private Integer errCode;
    private String message;
}

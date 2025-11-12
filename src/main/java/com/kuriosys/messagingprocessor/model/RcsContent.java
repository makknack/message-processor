package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kuriosys.messagingprocessor.enums.RcsContentType;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RcsContent {
    private RcsContentType rcsContentType;
}

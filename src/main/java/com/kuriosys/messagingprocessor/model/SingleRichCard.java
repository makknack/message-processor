package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SingleRichCard extends RcsContent{
    @JsonAlias("orientation")
    private String orientation; // Replace with CardOrientation enum if available

    @JsonAlias("content")
    private CardContent content;
}


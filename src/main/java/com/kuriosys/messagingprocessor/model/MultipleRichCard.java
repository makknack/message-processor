package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;

@Data
public class MultipleRichCard extends RcsContent{
    @JsonAlias("contents")
    private List<CardContent> contents;

    @JsonAlias("cardWidth")
    private String cardWidth; // Replace with CardWidth enum if available
}


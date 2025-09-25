package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardContent {
    @JsonAlias("cardTitle")
    private String cardTitle;

    @JsonAlias("cardDescription")
    private String cardDescription;

    @JsonAlias("mediaUrl")
    private String mediaUrl;

    @JsonAlias("thumbnailUrl")
    private String thumbnailUrl;

    @JsonAlias("mediaHeight")
    private String mediaHeight; // Replace with MediaHeight enum if available

    @JsonAlias("suggestions")
    private List<Suggestions> suggestions;
}


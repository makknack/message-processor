package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RcsPlainText extends RcsContent{
    @JsonAlias("text")
    private String text;

    @JsonAlias("suggestions")
    private List<Suggestions> suggestions;
}


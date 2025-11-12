package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.enums.RcsContentType;
import com.kuriosys.messagingprocessor.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RcsMessageTemplateService {

    private final ObjectMapper OBJECT_MAPPER;

    public Object formPersonalizedContentObject(Object templateContent, RcsContentType contentType,  Map<String, String> personalizedValues) throws JsonProcessingException {
        Object personalizedContent = null;
        if (RcsContentType.PLAIN_TEXT == contentType) {
            RcsPlainText plainText = OBJECT_MAPPER.convertValue(templateContent, RcsPlainText.class);
            String filledText = fillTemplateWithValues(plainText.getText(), personalizedValues);
            plainText.setText(filledText);
            personalizedContent = plainText;
        } else if (RcsContentType.RICH_CARD  ==  contentType) {
            SingleRichCard singleRichCard = OBJECT_MAPPER.convertValue(templateContent,SingleRichCard.class);
            CardContent content = singleRichCard.getContent();
            if (content != null) {
                if (content.getCardTitle() != null) {
                    content.setCardTitle(fillTemplateWithValues(content.getCardTitle(), personalizedValues));
                }
                if (content.getCardDescription() != null) {
                    content.setCardDescription(fillTemplateWithValues(content.getCardDescription(), personalizedValues));
                }
            }
            personalizedContent = singleRichCard;
        } else if (RcsContentType.CAROUSEL  == contentType) {
            MultipleRichCard carousel = OBJECT_MAPPER.convertValue(templateContent,MultipleRichCard.class);
            if (carousel.getContents() != null) {
                for (CardContent content : carousel.getContents()) {
                    if (content.getCardTitle() != null) {
                        content.setCardTitle(fillTemplateWithValues(content.getCardTitle(), personalizedValues));
                    }
                    if (content.getCardDescription() != null) {
                        content.setCardDescription(fillTemplateWithValues(content.getCardDescription(), personalizedValues));
                    }
                }
            }
            personalizedContent = carousel;
        }
        return personalizedContent;
    }

    private String fillTemplateWithValues(String template, Map<String, String> values) {
        if (template == null || values == null) return template;
        String result = template;
        log.info("Filling template: {} {}", template, values);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey().toLowerCase() + "}}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

}

package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kuriosys.messagingprocessor.enums.CardWidth;
import com.kuriosys.messagingprocessor.enums.RcsContentType;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.*;
import com.kuriosys.messagingprocessor.service.PayloadCreator;
import com.kuriosys.messagingprocessor.service.PayloadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JioPayloadCreator  implements  PayloadCreator{

    @Override
    public Map<String, Object> createPayload(PayloadRequest payloadRequest) throws ProcessingException {
        Map<String, Object> payload;
        Object content = payloadRequest.getContent();
        RcsContentType rcsContentType =payloadRequest.getContentType();
        if (rcsContentType == RcsContentType.PLAIN_TEXT) {
            payload = formPlainTextPayload((RcsPlainText) content);
        } else if (rcsContentType == RcsContentType.RICH_CARD) {
            payload = formSingleRichCardPayload((SingleRichCard) content);
        } else if(rcsContentType == RcsContentType.CAROUSEL) {
            payload = formMultiCardPayload((MultipleRichCard) content);
        } else {
            throw new ProcessingException("Unsupported content type: " + content.getClass().getName());
        }
        getHeaders(payloadRequest);
       return formTheFinalPayload(payloadRequest, payload,payloadRequest.getRecipients());
    }

    private Map<String, Object> formPlainTextPayload(RcsPlainText plainText) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("plainText", plainText.getText());
        if(plainText.getSuggestions() != null && !plainText.getSuggestions().isEmpty()) {
            payload.put("suggestions", buildSuggestions(plainText.getSuggestions()));
        }
        return payload;
    }

    private Map<String, Object> buildCardContentMap(CardContent cardContent) {
        Map<String, Object> cardMap = new java.util.HashMap<>();
        cardMap.put("cardTitle", cardContent.getCardTitle());
        cardMap.put("cardDescription", cardContent.getCardDescription());
        // Card Media
        Map<String, Object> cardMedia = new java.util.HashMap<>();
        cardMedia.put("mediaHeight", cardContent.getMediaHeight());
        Map<String, Object> contentInfo = new java.util.HashMap<>();
        contentInfo.put("fileUrl", cardContent.getMediaUrl());
        cardMedia.put("contentInfo", contentInfo);
        if (cardContent.getThumbnailUrl() != null) {
            cardMedia.put("thumbnailUrl", cardContent.getThumbnailUrl());
        }
        cardMap.put("cardMedia", cardMedia);

        //Suggestions
        if (cardContent.getSuggestions() != null) {
            List<Map<String, Object>> suggestionsList = buildSuggestions(cardContent.getSuggestions());
            cardMap.put("suggestions", suggestionsList);
        }
        return cardMap;
    }

    private List<Map<String, Object>> buildSuggestions(List<Suggestions> suggestions) {
        List<Map<String, Object>> suggestionsList = new java.util.ArrayList<>();
        for (var suggestion : suggestions) {
            com.kuriosys.messagingprocessor.enums.ActionType actionType = null;
            try {
                actionType = com.kuriosys.messagingprocessor.enums.ActionType.valueOf(suggestion.getActionType());
            } catch (Exception e) {
                // fallback for unknown action types
            }
            Map<String, Object> suggestionMap = new java.util.HashMap<>();
            if (actionType != null) {
                switch (actionType) {
                    case REPLY: {
                        if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null) {
                            throw new IllegalArgumentException("REPLY suggestion must have plainText and postBack data");
                        }
                        Map<String, Object> replyMap = new java.util.HashMap<>();
                        replyMap.put("plainText", suggestion.getDisplayText());
                        replyMap.put("postBack", Map.of("data", enrichPostBackData(suggestion.getPostbackData())));
                        suggestionMap.put("reply", replyMap);
                        break;
                    }
                    case OPEN_URL: {
                        if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null || suggestion.getActionUrl() == null) {
                            throw new IllegalArgumentException("OPEN_URL suggestion must have plainText, postBack, and openUrl");
                        }
                        Map<String, Object> actionMap = new java.util.HashMap<>();
                        actionMap.put("plainText", suggestion.getDisplayText());
                        actionMap.put("postBack", Map.of("data", enrichPostBackData(suggestion.getPostbackData())));
                        actionMap.put("openUrl", Map.of("url", suggestion.getActionUrl()));
                        suggestionMap.put("action", actionMap);
                        break;
                    }
                    case DIAL: {
                        if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null || suggestion.getPhoneNumber() == null) {
                            throw new IllegalArgumentException("DIAL suggestion must have plainText, postBack, and phoneNumber");
                        }
                        Map<String, Object> actionMap = new java.util.HashMap<>();
                        actionMap.put("plainText", suggestion.getDisplayText());
                        actionMap.put("postBack", Map.of("data", enrichPostBackData(suggestion.getPostbackData())));
                        actionMap.put("dialerAction", Map.of("phoneNumber", suggestion.getPhoneNumber()));
                        suggestionMap.put("action", actionMap);
                        break;
                    }
                    case VIEW_LOCATION: {
                        if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null || suggestion.getLocationLatitude() == null || suggestion.getLocationLongitude() == null || suggestion.getLocationLabel() == null) {
                            throw new IllegalArgumentException("VIEW_LOCATION suggestion must have plainText, postBack, locationLatitude, locationLongitude, and locationLabel");
                        }
                        Map<String, Object> actionMap = new java.util.HashMap<>();
                        actionMap.put("plainText", suggestion.getDisplayText());
                        actionMap.put("postBack", Map.of("data", enrichPostBackData(suggestion.getPostbackData())));
                        actionMap.put("showLocation", Map.of(
                            "coordinates", Map.of(
                                "latitude", suggestion.getLocationLatitude(),
                                "longitude", suggestion.getLocationLongitude()
                            ),
                            "label", suggestion.getLocationLabel()
                        ));
                        suggestionMap.put("action", actionMap);
                        break;
                    }
                    case CREATE_CALENDAR_EVENT: {
                        if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null || suggestion.getEventTitle() == null || suggestion.getEventDescription() == null || suggestion.getEventStartTime() == null || suggestion.getEventEndTime() == null) {
                            throw new IllegalArgumentException("CREATE_CALENDAR_EVENT suggestion must have plainText, postBack, eventTitle, eventDescription, eventStartTime, and eventEndTime");
                        }
                        Map<String, Object> actionMap = new java.util.HashMap<>();
                        actionMap.put("plainText", suggestion.getDisplayText());
                        actionMap.put("postBack", Map.of("data", enrichPostBackData(suggestion.getPostbackData())));
                        actionMap.put("createCalendarEvent", Map.of(
                                "startTime", suggestion.getEventStartTime(),
                                "endTime", suggestion.getEventEndTime(),
                                "title", suggestion.getEventTitle(),
                                "description", suggestion.getEventDescription()
                        ));
                        suggestionMap.put("action", actionMap);
                        break;
                    }
                    default:
                        // fallback for unknown action types
                        break;
                }
            }
            suggestionsList.add(suggestionMap);
        }
        return suggestionsList;
    }

    private Map<String, Object> formSingleRichCardPayload(SingleRichCard richCard) {
        return Map.of(
                "richCardDetails", Map.of(
                        "standalone", Map.of(
                                "cardOrientation", richCard.getOrientation(),
                                "content", buildCardContentMap(richCard.getContent())
                        )
                )
        );
    }


    private Map<String, Object> formTheFinalPayload(PayloadRequest payloadRequest, Map<String, Object> content, List<String> recipients) {
        Map<String, Object> data = Map.of(
                "content", content
        );
        return Map.of(
                "messageID", payloadRequest.getMessageRequestId(),
                "agentID", payloadRequest.getRcsExternalAgentId(),
                "contacts", recipients,
                "data", data
        );
    }

    private Map<String, Object> formMultiCardPayload(MultipleRichCard carousel) throws ProcessingException {
        List<Map<String, Object>> contentsList = new java.util.ArrayList<>();
        Map<String, Object> content = null;
        if (carousel.getContents() != null) {
            for (CardContent card : carousel.getContents()) {
                contentsList.add(buildCardContentMap(card));
            }
            content = Map.of(
                    "richCardDetails", Map.of(
                            "carousel", Map.of(
                                    "cardWidth", enrichCardWidth(carousel.getCardWidth()),
                                    "contents", contentsList
                            )
                    )
            );
        }
        return content;
    }

    private String enrichCardWidth(String width) throws ProcessingException {
        if (CardWidth.SMALL.name().equals(width)){
            return "SMALL_WIDTH";
        }
        else if(CardWidth.MEDIUM.name().equals(width)){
            return "MEDIUM_WIDTH";
        }
        else{
            throw new ProcessingException("Unsupported card width: " + width);
        }
    }

    private String enrichPostBackData(String postBackData) {
        return postBackData + "{{$50}}"; // In JIO payload {{$50}} must be appended to postBack data.
    }

    @Override
    public Map<String, String> getHeaders(PayloadRequest payloadRequest) {
        return Map.of(
                "Content-Type", "application/json",
                "x-apikey", payloadRequest.getServiceRouteDetails().getApiKey()
        );
    }
}

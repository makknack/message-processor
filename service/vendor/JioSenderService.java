public Map<String, Object> send(RcsPlainText plainText, String messageId, String agentId, List<String> recipients) {
    Map<String, Object> content = Map.of(
        "plainText", plainText.getText()
    );
    Map<String, Object> data = Map.of(
        "content", content
    );
    return Map.of(
        "messageID", messageId,
        "agentID", agentId,
        "contacts", recipients,
        "data", data
    );
}

public Map<String, Object> send(RcsMessageRequest rcsMessageRequest, String agentId, SingleRichCard richCard, List<String> recipients) {
    Map<String, Object> content = Map.of(
        "richCardDetails", Map.of(
            "standalone", Map.of(
                "cardOrientation", richCard.getOrientation().name(),
                "content", Map.of(
                    "cardTitle", richCard.getContent().getCardTitle(),
                    "cardDescription", richCard.getContent().getCardDescription(),
                    "cardMedia", Map.of(
                        "mediaHeight", richCard.getContent().getMediaHeight().name(),
                        "contentInfo", Map.of(
                            "fileUrl", richCard.getContent().getMediaUrl()
                        )
                    ),
                    "suggestions", richCard.getContent().getSuggestions() // Assuming suggestions is already a List<Map<String, Object>>
                )
            )
        )
    );
    Map<String, Object> data = Map.of(
        "content", content
    );
    return Map.of(
        "messageID", rcsMessageRequest.getMessageRequestId(),
        "agentID", agentId,
        "contacts", recipients,
        "data", data
    );
}

public Map<String, Object> formMultiCardPayload(MultipleRichCard carousel) {
    List<Map<String, Object>> contentsList = new java.util.ArrayList<>();
    if (carousel.getContents() != null) {
        for (CardContent card : carousel.getContents()) {
            List<Map<String, Object>> suggestionsList = new java.util.ArrayList<>();
            if (card.getSuggestions() != null) {
                for (var suggestion : card.getSuggestions()) {
                    Map<String, Object> actionMap = new java.util.HashMap<>();
                    actionMap.put("plainText", suggestion.getDisplayText());
                    if (suggestion.getPostbackData() != null) {
                        actionMap.put("postBack", Map.of("data", suggestion.getPostbackData()));
                    }
                    if (suggestion.getActionUrl() != null) {
                        actionMap.put("openUrl", Map.of("url", suggestion.getActionUrl()));
                    }
                    suggestionsList.add(actionMap);
                }
            }
            Map<String, Object> cardMap = Map.of(
                "cardTitle", card.getCardTitle(),
                "cardDescription", card.getCardDescription(),
                "cardMedia", Map.of(
                    "contentInfo", Map.of("fileUrl", card.getMediaUrl()),
                    "mediaHeight", card.getMediaHeight()
                ),
                "suggestions", suggestionsList
            );
            contentsList.add(cardMap);
        }
    }
    Map<String, Object> content = Map.of(
        "richCardDetails", Map.of(
            "carousel", Map.of(
                "cardWidth", carousel.getCardWidth(),
                "contents", contentsList
            )
        )
    );
    return content;
}

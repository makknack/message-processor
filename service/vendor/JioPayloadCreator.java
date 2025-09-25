                        case CREATE_CALENDAR_EVENT: {
                            // All fields mandatory: plainText, postBack, eventTitle, eventDescription, eventStartTime, eventEndTime
                            if (suggestion.getDisplayText() == null || suggestion.getPostbackData() == null || suggestion.getEventTitle() == null || suggestion.getEventDescription() == null || suggestion.getEventStartTime() == null || suggestion.getEventEndTime() == null) {
                                throw new IllegalArgumentException("CREATE_CALENDAR_EVENT suggestion must have plainText, postBack, eventTitle, eventDescription, eventStartTime, and eventEndTime");
                            }
                            Map<String, Object> actionMap = new java.util.HashMap<>();
                            actionMap.put("plainText", suggestion.getDisplayText());
                            actionMap.put("postBack", Map.of("data", suggestion.getPostbackData()));
                            actionMap.put("createCalendarEvent", Map.of(
                                    "startTime", suggestion.getEventStartTime(),
                                    "endTime", suggestion.getEventEndTime(),
                                    "title", suggestion.getEventTitle(),
                                    "description", suggestion.getEventDescription()
                            ));
                            suggestionMap.put("action", actionMap);
                            break;
                        }




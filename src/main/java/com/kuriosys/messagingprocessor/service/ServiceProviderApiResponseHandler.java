package com.kuriosys.messagingprocessor.service;

public interface ServiceProviderApiResponseHandler {
    String handle(String response) throws Exception;
}

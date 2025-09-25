package com.kuriosys.messagingprocessor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/kafka")
public class KafkaManagementController {

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @PostMapping("/pause-listener/{listenerId}")
    public String pauseListener(String listenerId) {
        var container =  kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if(Objects.nonNull(container)) {
          container.pause();
        }
        return "Listener " + listenerId + " paused.";
    }

    @PostMapping("/resume-listener/{listenerId}")
    public String resumeListener(String listenerId) {
        var container =  kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if(Objects.nonNull(container)) {
            container.resume();
        }
        return "Listener " + listenerId + "resumed.";
    }

    @PostMapping("/start-listener/{listenerId}")
    public String startListener(String listenerId) {
        var container =  kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if(Objects.nonNull(container)) {
            container.start();
        }
        return "Listener " + listenerId + " started.";
    }

    @PostMapping("/stop-listener/{listenerId}")
    public String stopListener(String listenerId) {
        var container =  kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if(Objects.nonNull(container)) {
            container.stop();
        }
        return "Listener " + listenerId + " stopped.";
    }

    @GetMapping("/listener-status/{listenerId}")
    public String getListenerStatus(String listenerId) {
        var container =  kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
        if(Objects.nonNull(container)) {
            return "Listener " + listenerId + " is " + (container.isRunning() ? "running" : "stopped") + " and is " + (container.isContainerPaused() ? "paused" : "resumed") + ".";
        }
        return "Listener " + listenerId + " not found.";
    }

    @GetMapping("/kafka/listeners/running")
    public List<String> getRunningListeners() {
        return kafkaListenerEndpointRegistry.getListenerContainers().stream()
                .filter(Lifecycle::isRunning)
                .map(MessageListenerContainer::getListenerId)
                .collect(Collectors.toList());
    }


}

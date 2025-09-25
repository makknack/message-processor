package com.kuriosys.messagingprocessor.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class ConsumerReBalanceListener implements ConsumerAwareRebalanceListener {

    @Override
    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
       log.warn("Partitions revoked before commit: {}", partitions);
    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        log.warn("Partitions assigned: {}", partitions);
    }

    @Override
    public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        log.warn("Partitions revoked after commit: {}", partitions);
    }
}

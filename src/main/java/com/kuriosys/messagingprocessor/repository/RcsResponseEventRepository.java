package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsResponseEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsResponseEventRepository extends JpaRepository<RcsResponseEvent, Integer> {
    // You can add custom query methods here if needed
}


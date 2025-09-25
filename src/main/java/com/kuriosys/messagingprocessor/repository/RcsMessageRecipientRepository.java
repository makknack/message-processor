package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsMessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsMessageRecipientRepository extends JpaRepository<RcsMessageRecipient, Integer> {
    // Add custom query methods if needed
}


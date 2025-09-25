package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsInvalidRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsInvalidRecipientRepository extends JpaRepository<RcsInvalidRecipient, Integer> {
}


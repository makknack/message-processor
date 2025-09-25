package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsMessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsMessageTemplateRepository extends JpaRepository<RcsMessageTemplate, String> {
    // Additional query methods if needed
}


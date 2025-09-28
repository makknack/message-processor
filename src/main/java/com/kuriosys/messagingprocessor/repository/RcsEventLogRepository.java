package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RcsEventLogRepository extends JpaRepository<RcsEventLog, Long> {
    // Add custom query methods if needed
}


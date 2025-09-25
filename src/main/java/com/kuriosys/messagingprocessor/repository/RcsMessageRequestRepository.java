package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.enums.RcsMessageRequestStatus;
import com.kuriosys.messagingprocessor.model.RcsMessageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RcsMessageRequestRepository extends JpaRepository<RcsMessageRequest, String> {
    Optional<RcsMessageRequest> findByMessageRequestId(String messageRequestId);
    Optional<RcsMessageRequest> findByMessageRequestIdAndStatusIn(String messageRequestId, Collection<RcsMessageRequestStatus> statuses);
}
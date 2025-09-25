package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.enums.RcsExternalAgentStatus;
import com.kuriosys.messagingprocessor.model.RcsExternalAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface RcsExternalAgentRepository extends JpaRepository<RcsExternalAgent, BigInteger> {
    Optional<RcsExternalAgent> findByAgentIdAndExternalAgentStatus(String agentId, RcsExternalAgentStatus status);
}

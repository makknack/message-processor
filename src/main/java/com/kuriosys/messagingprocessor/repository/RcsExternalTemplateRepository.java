package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.RcsExternalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RcsExternalTemplateRepository extends JpaRepository<RcsExternalTemplate, Long> {

    Optional<RcsExternalTemplate> findByExternalTemplateId(String externalTemplateId);

    List<RcsExternalTemplate> findByServiceRouteId(String serviceRouteId);

    Optional<RcsExternalTemplate> findByMessageTemplateId(String messageTemplateId);

    Optional<RcsExternalTemplate> findByServiceRouteIdAndMessageTemplateId(String serviceRouteId, String messageTemplateId);
}


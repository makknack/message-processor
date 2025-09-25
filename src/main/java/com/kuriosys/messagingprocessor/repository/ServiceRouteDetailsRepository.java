package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.enums.ServiceRouteType;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface ServiceRouteDetailsRepository  extends JpaRepository<ServiceRouteDetails, String> {
    @Query("SELECT srd FROM ServiceRouteDetails srd JOIN UserServiceRoute usr ON srd.serviceRouteId = usr.serviceRouteId WHERE usr.userId = :userId AND srd.serviceRouteType = :serviceRouteType")
    Optional<ServiceRouteDetails> findServiceRouteDetailsByUserIdAndType(@Param("userId") BigInteger userId, @Param("serviceRouteType") ServiceRouteType serviceRouteType);
}

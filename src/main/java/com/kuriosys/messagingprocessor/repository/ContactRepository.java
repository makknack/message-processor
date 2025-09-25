package com.kuriosys.messagingprocessor.repository;

import com.kuriosys.messagingprocessor.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.contactGroupId = :contactGroupId")
    long countByGroupId(@Param("contactGroupId") String groupId);

    @Query(value = "SELECT c.mobileNumber FROM Contact c WHERE c.contactGroupId = :contactGroupId ORDER BY c.createdAt ASC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY")
    List<String> findNumbersByGroupIdOrdered(@Param("contactGroupId") String groupId, @Param("offset") long offset, @Param("limit") int limit);
}

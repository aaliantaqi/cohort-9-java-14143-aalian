package com.tenpearls.contactmanagementsystem.repositories;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact, String> {
    Page<Contact> findByOwnerId(int userId, Pageable pageable);
    Optional<Contact> findByIdAndOwnerId(String id, int userId);

    @Query("SELECT c FROM Contact c WHERE c.owner.id = :ownerId " +
            "AND (LOWER(c.firstname) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastname) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contact> searchByOwnerIdAndNameContainingIgnoreCase(@Param("ownerId") Integer ownerId,
                                                             @Param("search") String search,
                                                             Pageable pageable);
}
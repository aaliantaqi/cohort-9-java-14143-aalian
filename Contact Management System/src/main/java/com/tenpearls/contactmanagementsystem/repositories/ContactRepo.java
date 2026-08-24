package com.tenpearls.contactmanagementsystem.repositories;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact, String> {
    Page<Contact> findByOwnerId(int userId, Pageable pageable);
    Optional<Contact> findByIdAndOwnerId(String id, int userId);
    Page<Contact> findByOwnerIdAndNameContainingIgnoreCase(Integer ownerId, String name, Pageable pageable);

}


package com.tenpearls.contactmanagementsystem.repositories;

import com.tenpearls.contactmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findByPhone(String phone);

    // Login/lookup can happen with either identifier - tries email first, then phone.
    // Used everywhere we previously called findByUsername(...).
    default User findByEmailOrPhone(String identifier) {
        User user = findByEmail(identifier);
        return user != null ? user : findByPhone(identifier);
    }
}
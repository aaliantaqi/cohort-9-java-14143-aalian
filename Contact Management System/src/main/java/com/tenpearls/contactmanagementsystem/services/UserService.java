package com.tenpearls.contactmanagementsystem.services;

import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.model.UserRegistrationRequest;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.Objects;

import java.util.List;

@Service
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public User getUser(Integer id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User addUser(UserRegistrationRequest request){
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean hasPhone = request.getPhone() != null && !request.getPhone().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Please provide an email or a phone number");
        }
        if (hasEmail && (userRepository.findByEmail(request.getEmail()) != null
                || userRepository.findByPhone(request.getEmail()) != null)) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (hasPhone && (userRepository.findByPhone(request.getPhone()) != null
                || userRepository.findByEmail(request.getPhone()) != null)) {
            throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(hasEmail ? request.getEmail() : null);
        user.setPhone(hasPhone ? request.getPhone() : null);
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email or phone already exists");
        }
    }

    public void deleteUser(Integer id){
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    public User updateUser(Integer id, User updatedData) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        String normalizedEmail = (updatedData.getEmail() != null && !updatedData.getEmail().isBlank())
                ? updatedData.getEmail() : null;
        String normalizedPhone = (updatedData.getPhone() != null && !updatedData.getPhone().isBlank())
                ? updatedData.getPhone() : null;

        if (normalizedEmail == null && normalizedPhone == null) {
            throw new IllegalArgumentException("Please provide an email or a phone number");
        }

        if (normalizedEmail != null) {
            User conflict = userRepository.findByEmail(normalizedEmail);
            if (conflict == null) {
                conflict = userRepository.findByPhone(normalizedEmail);
            }
            if (conflict != null && !Objects.equals(conflict.getId(), id)) {
                throw new IllegalArgumentException("Email already in use: " + normalizedEmail);
            }
        }
        if (normalizedPhone != null) {
            User conflict = userRepository.findByPhone(normalizedPhone);
            if (conflict == null) {
                conflict = userRepository.findByEmail(normalizedPhone);
            }
            if (conflict != null && !Objects.equals(conflict.getId(), id)) {
                throw new IllegalArgumentException("Phone already in use: " + normalizedPhone);
            }
        }

        existingUser.setFirstname(updatedData.getFirstname());
        existingUser.setLastname(updatedData.getLastname());
        existingUser.setEmail(normalizedEmail);
        existingUser.setPhone(normalizedPhone);

        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(updatedData.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public User getUserByUsername(String identifier) {
        User user = userRepository.findByEmailOrPhone(identifier);
        if (user == null) {
            throw new RuntimeException("User not found: " + identifier);
        }
        return user;
    }

    public void changePassword(String identifier, String currentPassword, String newPassword) {
        User user = getUserByUsername(identifier);

        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
package com.tenpearls.contactmanagementsystem.services;

import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.model.UserRegistrationRequest;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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

    public User updateUser(Integer id, User updatedData) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        User userWithSameUsername = userRepository.findByUsername(updatedData.getUsername());
        if (userWithSameUsername != null && userWithSameUsername.getId() != id) {
            throw new IllegalArgumentException("Username already exists: " + updatedData.getUsername());
        }

        existingUser.setFirstname(updatedData.getFirstname());
        existingUser.setLastname(updatedData.getLastname());
        existingUser.setUsername(updatedData.getUsername());

        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(updatedData.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer id){
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


    public User addUser(UserRegistrationRequest request){
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
    }
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);

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
package com.tenpearls.contactmanagementsystem.controllers;

import com.tenpearls.contactmanagementsystem.model.LoginRequest;
import com.tenpearls.contactmanagementsystem.model.UserRegistrationRequest;
import com.tenpearls.contactmanagementsystem.services.UserService;
import com.tenpearls.contactmanagementsystem.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;


import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/users")
    public List<User> getUsers(){
        return userService.getUsers();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Integer id){
        try {
            User user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@RequestBody User user, @PathVariable("id") Integer id){
        try {
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<String> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        return ResponseEntity.ok(token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<?> newUser(@RequestBody UserRegistrationRequest request){
        try {
            User newUser = userService.addUser(request);
            String identifier = newUser.getEmail() != null ? newUser.getEmail() : newUser.getPhone();
            logger.info("New user registered: '{}'", identifier);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (IllegalArgumentException e) {
            String attempted = request.getEmail() != null ? request.getEmail() : request.getPhone();
            logger.warn("Registration failed for '{}': {}", attempted, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Integer id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByUsername(authentication.getName());
        // Build a response without the password hash
        var profile = new java.util.HashMap<String, Object>();
        profile.put("id", user.getId());
        profile.put("firstname", user.getFirstname());
        profile.put("lastname", user.getLastname());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody java.util.Map<String, String> body, Authentication authentication) {
        try {
            userService.changePassword(authentication.getName(), body.get("currentPassword"), body.get("newPassword"));
            logger.info("User '{}' changed their password", authentication.getName());
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Password change failed for '{}': {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String identifier = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "unknown";
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        logger.info("User '{}' logged out", identifier);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        try {
            Authentication authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword());
            Authentication authResult = authenticationManager.authenticate(authRequest);

            request.getSession().invalidate();
            request.getSession(true);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            logger.info("User '{}' logged in successfully", loginRequest.getIdentifier());
            return ResponseEntity.ok("Login was successful!");
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for '{}'", loginRequest.getIdentifier());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unknown error occurred");
        }
    }
}
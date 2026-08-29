package com.tenpearls.contactmanagementsystem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpearls.contactmanagementsystem.model.LoginRequest;
import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.model.UserRegistrationRequest;
import com.tenpearls.contactmanagementsystem.security.SecurityConfig;
import com.tenpearls.contactmanagementsystem.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // ---------- GET /api/users ----------

    @Test
    @WithMockUser
    void getUsers_returnsListOfUsers() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("testuser@example.com");

        when(userService.getUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("testuser@example.com"));
    }

    // ---------- GET /api/user/{id} ----------

    @Test
    @WithMockUser
    void getUser_returns200_whenFound() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("testuser@example.com");

        when(userService.getUser(1)).thenReturn(user);

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    @WithMockUser
    void getUser_returns404_whenNotFound() throws Exception {
        when(userService.getUser(999)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/user/999"))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/user/{id} ----------

    @Test
    @WithMockUser
    void updateUser_returns200_whenSuccessful() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("updateduser@example.com");

        when(userService.updateUser(eq(1), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/user/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updateduser@example.com"));
    }

    @Test
    @WithMockUser
    void updateUser_returns409_whenEmailConflict() throws Exception {
        User updatedUser = new User();
        updatedUser.setEmail("taken@example.com");

        when(userService.updateUser(eq(1), any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already in use: taken@example.com"));

        mockMvc.perform(put("/api/user/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void updateUser_returns404_whenUserNotFound() throws Exception {
        User updatedUser = new User();
        updatedUser.setEmail("someone@example.com");

        when(userService.updateUser(eq(999), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/user/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /api/register ----------

    @Test
    void newUser_returns201_whenRegistrationSucceeds() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstname("New");
        request.setLastname("User");

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setEmail("newuser@example.com");

        when(userService.addUser(any(UserRegistrationRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void newUser_returns201_whenRegisteringWithPhoneOnly() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setPhone("03001234567");
        request.setPassword("password123");
        request.setFirstname("New");
        request.setLastname("User");

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setPhone("03001234567");

        when(userService.addUser(any(UserRegistrationRequest.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("03001234567"));
    }

    @Test
    void newUser_returns409_whenEmailAlreadyExists() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existinguser@example.com");
        request.setPassword("password123");

        when(userService.addUser(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists: existinguser@example.com"));

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void newUser_returns409_whenNeitherEmailNorPhoneProvided() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setPassword("password123");
        request.setFirstname("New");
        request.setLastname("User");

        when(userService.addUser(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Please provide an email or a phone number"));

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ---------- DELETE /api/user/{id} ----------

    @Test
    @WithMockUser
    void deleteUser_returns204_whenSuccessful() throws Exception {
        doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/user/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUser_returns404_whenNotFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(999);

        mockMvc.perform(delete("/api/user/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/me ----------

    @Test
    @WithMockUser(username = "testuser@example.com")
    void getCurrentUser_returnsProfile_whenAuthenticated() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("testuser@example.com");
        user.setFirstname("Test");
        user.setLastname("User");

        when(userService.getUserByUsername("testuser@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.firstname").value("Test"));
    }

    // ---------- PUT /api/change-password ----------

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_returns200_whenSuccessful() throws Exception {
        doNothing().when(userService).changePassword("testuser@example.com", "oldpass", "newpass");

        String body = "{\"currentPassword\":\"oldpass\",\"newPassword\":\"newpass\"}";

        mockMvc.perform(put("/api/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_returns400_whenCurrentPasswordIncorrect() throws Exception {
        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).changePassword("testuser@example.com", "wrongpass", "newpass");

        String body = "{\"currentPassword\":\"wrongpass\",\"newPassword\":\"newpass\"}";

        mockMvc.perform(put("/api/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/login ----------

    @Test
    void login_returns200_whenCredentialsValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("testuser@example.com");
        loginRequest.setPassword("correctpassword");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "testuser@example.com", "correctpassword", List.of()));

        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void login_returns200_whenLoggingInWithPhone() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("03001234567");
        loginRequest.setPassword("correctpassword");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "03001234567", "correctpassword", List.of()));

        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("testuser@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- POST /api/logout ----------

    @Test
    @WithMockUser
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/api/logout").with(csrf()))
                .andExpect(status().isOk());
    }
}
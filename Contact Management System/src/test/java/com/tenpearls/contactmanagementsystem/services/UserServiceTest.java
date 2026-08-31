package com.tenpearls.contactmanagementsystem.services;

import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.model.UserRegistrationRequest;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("testuser@example.com");
        existingUser.setFirstname("Test");
        existingUser.setLastname("User");
        existingUser.setPassword("encoded-old-password");
    }

    // ---------- getUsers ----------

    @Test
    void getUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<User> result = userService.getUsers();

        assertEquals(1, result.size());
        assertEquals("testuser@example.com", result.get(0).getEmail());
    }

    // ---------- getUser ----------

    @Test
    void getUser_returnsUser_whenFound() {
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        User result = userService.getUser(1);

        assertEquals("testuser@example.com", result.getEmail());
    }

    @Test
    void getUser_throwsException_whenNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUser(999));
    }

    // ---------- updateUser ----------

    @Test
    void updateUser_updatesFields_whenNoEmailOrPhoneConflict() {
        User updatedData = new User();
        updatedData.setFirstname("Updated");
        updatedData.setLastname("Name");
        updatedData.setEmail("testuser@example.com");
        updatedData.setPassword("");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(1, updatedData);

        assertEquals("Updated", result.getFirstname());
        assertEquals("Name", result.getLastname());
    }

    @Test
    void updateUser_throwsException_whenNewEmailAlreadyTakenByAnotherUser() {
        User updatedData = new User();
        updatedData.setEmail("taken@example.com");

        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setEmail("taken@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1, updatedData));
    }

    @Test
    void updateUser_throwsException_whenNewPhoneAlreadyTakenByAnotherUser() {
        User updatedData = new User();
        updatedData.setPhone("03001234567");

        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setPhone("03001234567");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByPhone("03001234567")).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1, updatedData));
    }

    @Test
    void updateUser_encodesNewPassword_whenPasswordProvided() {
        User updatedData = new User();
        updatedData.setEmail("testuser@example.com");
        updatedData.setFirstname("Test");
        updatedData.setLastname("User");
        updatedData.setPassword("newPlainPassword");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(existingUser);
        when(bCryptPasswordEncoder.encode("newPlainPassword")).thenReturn("encoded-new-password");
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updateUser(1, updatedData);

        assertEquals("encoded-new-password", result.getPassword());
    }

    // ---------- deleteUser ----------

    @Test
    void deleteUser_deletes_whenUserExists() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteUser(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_throwsException_whenUserDoesNotExist() {
        when(userRepository.existsById(999)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(999));
        verify(userRepository, never()).deleteById(any());
    }

    // ---------- addUser (registration) ----------

    @Test
    void addUser_createsUser_whenEmailIsAvailable() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstname("New");
        request.setLastname("User");
        request.setEmail("newuser@example.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(null);
        when(bCryptPasswordEncoder.encode("plainPassword")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addUser(request);

        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
    }

    @Test
    void addUser_createsUser_whenOnlyPhoneProvided() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstname("New");
        request.setLastname("User");
        request.setPhone("03001234567");
        request.setPassword("plainPassword");

        when(userRepository.findByPhone("03001234567")).thenReturn(null);
        when(bCryptPasswordEncoder.encode("plainPassword")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addUser(request);

        assertEquals("03001234567", result.getPhone());
        assertNull(result.getEmail());
    }

    @Test
    void addUser_throwsException_whenNeitherEmailNorPhoneProvided() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstname("New");
        request.setLastname("User");
        request.setPassword("plainPassword");

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsException_whenEmailAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("testuser@example.com");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(existingUser);

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsException_whenPhoneAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setPhone("03001234567");

        User otherUser = new User();
        otherUser.setPhone("03001234567");
        when(userRepository.findByPhone("03001234567")).thenReturn(otherUser);

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsException_whenDatabaseConstraintViolated() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(null);
        when(bCryptPasswordEncoder.encode("plainPassword")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
    }

    // ---------- getUserByUsername (looks up by email or phone) ----------

    @Test
    void getUserByUsername_returnsUser_whenFoundByEmail() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(existingUser);

        User result = userService.getUserByUsername("testuser@example.com");

        assertEquals("testuser@example.com", result.getEmail());
    }

    @Test
    void getUserByUsername_throwsException_whenNotFound() {
        when(userRepository.findByEmailOrPhone("ghost")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> userService.getUserByUsername("ghost"));
    }

    // ---------- changePassword ----------

    @Test
    void changePassword_updatesPassword_whenCurrentPasswordMatches() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(existingUser);
        when(bCryptPasswordEncoder.matches("oldPassword", "encoded-old-password")).thenReturn(true);
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encoded-new-password");

        userService.changePassword("testuser@example.com", "oldPassword", "newPassword");

        assertEquals("encoded-new-password", existingUser.getPassword());
        verify(userRepository).save(existingUser);
    }

    @Test
    void changePassword_throwsException_whenCurrentPasswordIncorrect() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(existingUser);
        when(bCryptPasswordEncoder.matches("wrongPassword", "encoded-old-password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("testuser@example.com", "wrongPassword", "newPassword"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_throwsException_whenNewPasswordIsBlank() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(existingUser);
        when(bCryptPasswordEncoder.matches("oldPassword", "encoded-old-password")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("testuser@example.com", "oldPassword", "  "));

        verify(userRepository, never()).save(any());
    }
}
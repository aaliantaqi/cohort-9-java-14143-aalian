package com.tenpearls.contactmanagementsystem.repositories;

import com.tenpearls.contactmanagementsystem.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenExists() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("encoded-password");
        userRepository.save(user);

        User result = userRepository.findByEmail("testuser@example.com");

        assertNotNull(result);
        assertEquals("testuser@example.com", result.getEmail());
        assertEquals("Test", result.getFirstname());
    }

    @Test
    void findByEmail_returnsNull_whenNotFound() {
        User result = userRepository.findByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void findByPhone_returnsUser_whenExists() {
        User user = new User();
        user.setPhone("03001234567");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("encoded-password");
        userRepository.save(user);

        User result = userRepository.findByPhone("03001234567");

        assertNotNull(result);
        assertEquals("03001234567", result.getPhone());
        assertEquals("Test", result.getFirstname());
    }

    @Test
    void findByPhone_returnsNull_whenNotFound() {
        User result = userRepository.findByPhone("00000000000");

        assertNull(result);
    }

    @Test
    void findByEmailOrPhone_findsUser_byEmail() {
        User user = new User();
        user.setEmail("byemail@example.com");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("encoded-password");
        userRepository.save(user);

        User result = userRepository.findByEmailOrPhone("byemail@example.com");

        assertNotNull(result);
        assertEquals("byemail@example.com", result.getEmail());
    }

    @Test
    void findByEmailOrPhone_findsUser_byPhone_whenEmailDoesNotMatch() {
        User user = new User();
        user.setPhone("03111234567");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPassword("encoded-password");
        userRepository.save(user);

        User result = userRepository.findByEmailOrPhone("03111234567");

        assertNotNull(result);
        assertEquals("03111234567", result.getPhone());
    }

    @Test
    void findByEmailOrPhone_returnsNull_whenNeitherMatches() {
        User result = userRepository.findByEmailOrPhone("no-such-identifier");

        assertNull(result);
    }

    @Test
    void save_persistsUserWithGeneratedId() {
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setFirstname("New");
        user.setLastname("User");
        user.setPassword("encoded-password");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
    }
}
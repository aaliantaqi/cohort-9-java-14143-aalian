package com.tenpearls.contactmanagementsystem.repositories;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import com.tenpearls.contactmanagementsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ContactRepoTest {

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherOwner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner1@example.com");
        owner.setFirstname("Owner");
        owner.setLastname("One");
        owner.setPassword("encoded-password");
        owner = userRepository.save(owner);

        otherOwner = new User();
        otherOwner.setEmail("owner2@example.com");
        otherOwner.setFirstname("Owner");
        otherOwner.setLastname("Two");
        otherOwner.setPassword("encoded-password");
        otherOwner = userRepository.save(otherOwner);
    }

    private Contact newContact(String firstname, String lastname, User forOwner) {
        Contact contact = new Contact();
        contact.setFirstname(firstname);
        contact.setLastname(lastname);
        contact.setAddress("123 Main St");
        contact.setTitle("Manager");
        contact.setStatus("Active");
        contact.setOwner(forOwner);

        Contact.LabeledEmail email = new Contact.LabeledEmail();
        email.setLabel("Work");
        email.setEmail((firstname + "." + lastname).toLowerCase().replace(" ", ".") + "@example.com");
        contact.setEmails(List.of(email));

        Contact.LabeledPhone phone = new Contact.LabeledPhone();
        phone.setLabel("Work");
        phone.setPhone("1234567890");
        contact.setPhones(List.of(phone));

        return contact;
    }

    @Test
    void findByOwnerId_returnsOnlyContactsBelongingToThatOwner() {
        contactRepo.save(newContact("Alice", "Anderson", owner));
        contactRepo.save(newContact("Bob", "Baker", owner));
        contactRepo.save(newContact("Charlie", "Chen", otherOwner));

        Page<Contact> result = contactRepo.findByOwnerId(owner.getId(), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(c -> c.getOwner().getId() == owner.getId()));
    }

    @Test
    void findByOwnerId_returnsEmptyPage_whenOwnerHasNoContacts() {
        Page<Contact> result = contactRepo.findByOwnerId(owner.getId(), PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByIdAndOwnerId_returnsContact_whenOwnedByCorrectUser() {
        Contact saved = contactRepo.save(newContact("Alice", "Anderson", owner));

        Optional<Contact> result = contactRepo.findByIdAndOwnerId(saved.getId(), owner.getId());

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getFirstname());
        assertEquals("Anderson", result.get().getLastname());
    }

    @Test
    void findByIdAndOwnerId_returnsEmpty_whenOwnedByDifferentUser() {
        Contact saved = contactRepo.save(newContact("Alice", "Anderson", owner));

        Optional<Contact> result = contactRepo.findByIdAndOwnerId(saved.getId(), otherOwner.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndOwnerId_returnsEmpty_whenIdDoesNotExist() {
        Optional<Contact> result = contactRepo.findByIdAndOwnerId("nonexistent-id", owner.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByOwnerIdAndNameContainingIgnoreCase_findsMatchingContacts_caseInsensitive() {
        contactRepo.save(newContact("Alice", "Johnson", owner));
        contactRepo.save(newContact("Bob", "Smith", owner));
        contactRepo.save(newContact("alice", "Brown", owner));

        Page<Contact> result = contactRepo.searchByOwnerIdAndNameContainingIgnoreCase(
                owner.getId(), "alice", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void searchByOwnerIdAndNameContainingIgnoreCase_matchesOnLastname() {
        contactRepo.save(newContact("Bob", "Smithson", owner));
        contactRepo.save(newContact("Alice", "Johnson", owner));

        Page<Contact> result = contactRepo.searchByOwnerIdAndNameContainingIgnoreCase(
                owner.getId(), "smith", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Smithson", result.getContent().get(0).getLastname());
    }

    @Test
    void searchByOwnerIdAndNameContainingIgnoreCase_returnsEmpty_whenNoMatch() {
        contactRepo.save(newContact("Bob", "Smith", owner));

        Page<Contact> result = contactRepo.searchByOwnerIdAndNameContainingIgnoreCase(
                owner.getId(), "xyz", PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchByOwnerIdAndNameContainingIgnoreCase_doesNotReturnOtherOwnersContacts() {
        contactRepo.save(newContact("Alice", "Johnson", owner));
        contactRepo.save(newContact("Alice", "Williams", otherOwner));

        Page<Contact> result = contactRepo.searchByOwnerIdAndNameContainingIgnoreCase(
                owner.getId(), "alice", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Johnson", result.getContent().get(0).getLastname());
    }
}
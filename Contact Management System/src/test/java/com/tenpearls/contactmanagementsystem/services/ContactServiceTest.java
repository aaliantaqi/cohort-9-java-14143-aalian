package com.tenpearls.contactmanagementsystem.services;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.repositories.ContactRepo;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepo contactRepo;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("testuser@example.com");
    }

    private Contact.LabeledEmail labeledEmail(String label, String email) {
        Contact.LabeledEmail e = new Contact.LabeledEmail();
        e.setLabel(label);
        e.setEmail(email);
        return e;
    }

    private Contact.LabeledPhone labeledPhone(String label, String phone) {
        Contact.LabeledPhone p = new Contact.LabeledPhone();
        p.setLabel(label);
        p.setPhone(phone);
        return p;
    }

    // ---------- getAllContacts ----------

    @Test
    void getAllContacts_throwsException_whenPageIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> contactService.getAllContacts("testuser@example.com", -1, 10, null));
    }

    @Test
    void getAllContacts_throwsException_whenSizeIsZero() {
        assertThrows(IllegalArgumentException.class,
                () -> contactService.getAllContacts("testuser@example.com", 0, 0, null));
    }

    @Test
    void getAllContacts_throwsException_whenSizeExceeds100() {
        assertThrows(IllegalArgumentException.class,
                () -> contactService.getAllContacts("testuser@example.com", 0, 101, null));
    }

    @Test
    void getAllContacts_returnsPagedContacts_whenNoSearchTerm() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        Page<Contact> fakePage = new PageImpl<>(List.of(new Contact()));
        when(contactRepo.findByOwnerId(eq(1), any(PageRequest.class))).thenReturn(fakePage);

        Page<Contact> result = contactService.getAllContacts("testuser@example.com", 0, 10, null);

        assertEquals(1, result.getTotalElements());
        verify(contactRepo).findByOwnerId(eq(1), any(PageRequest.class));
    }

    @Test
    void getAllContacts_usesSearch_whenSearchTermProvided() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        Page<Contact> fakePage = new PageImpl<>(List.of(new Contact()));
        when(contactRepo.searchByOwnerIdAndNameContainingIgnoreCase(eq(1), eq("john"), any(PageRequest.class)))
                .thenReturn(fakePage);

        Page<Contact> result = contactService.getAllContacts("testuser@example.com", 0, 10, "john");

        assertEquals(1, result.getTotalElements());
        verify(contactRepo).searchByOwnerIdAndNameContainingIgnoreCase(eq(1), eq("john"), any(PageRequest.class));
    }

    // ---------- getContact ----------

    @Test
    void getContact_returnsContact_whenFoundAndOwnedByUser() {
        Contact contact = new Contact();
        contact.setId("abc-123");

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("abc-123", 1)).thenReturn(Optional.of(contact));

        Contact result = contactService.getContact("abc-123", "testuser@example.com");

        assertEquals("abc-123", result.getId());
    }

    @Test
    void getContact_throwsException_whenNotFound() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("missing-id", 1)).thenReturn(Optional.empty());

        assertThrows(ContactService.ContactNotFoundException.class,
                () -> contactService.getContact("missing-id", "testuser@example.com"));
    }

    @Test
    void getContact_throwsException_whenUserNotFound() {
        when(userRepository.findByEmailOrPhone("ghost")).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> contactService.getContact("abc-123", "ghost"));
    }

    // ---------- createContact ----------

    @Test
    void createContact_setsOwnerAndSaves() {
        Contact newContact = new Contact();
        Contact savedContact = new Contact();
        savedContact.setId("new-id");

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.save(newContact)).thenReturn(savedContact);

        Contact result = contactService.createContact(newContact, "testuser@example.com");

        assertEquals(testUser, newContact.getOwner());
        assertEquals("new-id", result.getId());
        verify(contactRepo).save(newContact);
    }

    @Test
    void createContact_savesWithEmailsAndPhones() {
        Contact newContact = new Contact();
        newContact.setEmails(List.of(labeledEmail("Work", "new@example.com")));
        newContact.setPhones(List.of(labeledPhone("Home", "12345")));

        Contact savedContact = new Contact();
        savedContact.setId("new-id");
        savedContact.setEmails(newContact.getEmails());
        savedContact.setPhones(newContact.getPhones());

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.save(newContact)).thenReturn(savedContact);

        Contact result = contactService.createContact(newContact, "testuser@example.com");

        assertEquals(1, result.getEmails().size());
        assertEquals("new@example.com", result.getEmails().get(0).getEmail());
        assertEquals(1, result.getPhones().size());
        assertEquals("12345", result.getPhones().get(0).getPhone());
    }

    // ---------- deleteContact ----------

    @Test
    void deleteContact_deletesWhenFoundAndOwned() {
        Contact contact = new Contact();
        contact.setId("abc-123");

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("abc-123", 1)).thenReturn(Optional.of(contact));

        contactService.deleteContact("abc-123", "testuser@example.com");

        verify(contactRepo).delete(contact);
    }

    @Test
    void deleteContact_throwsException_whenNotFound() {
        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("missing-id", 1)).thenReturn(Optional.empty());

        assertThrows(ContactService.ContactNotFoundException.class,
                () -> contactService.deleteContact("missing-id", "testuser@example.com"));

        verify(contactRepo, never()).delete(any());
    }

    // ---------- updateContact ----------

    @Test
    void updateContact_updatesBasicFieldsAndSaves() {
        Contact existing = new Contact();
        existing.setId("abc-123");
        existing.setFirstname("Old");
        existing.setLastname("Name");

        Contact updatedData = new Contact();
        updatedData.setFirstname("New");
        updatedData.setLastname("Name2");
        updatedData.setTitle("Manager");
        updatedData.setStatus("Active");
        updatedData.setAddress("New Address");

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("abc-123", 1)).thenReturn(Optional.of(existing));
        when(contactRepo.save(existing)).thenReturn(existing);

        Contact result = contactService.updateContact("abc-123", updatedData, "testuser@example.com");

        assertEquals("New", result.getFirstname());
        assertEquals("Name2", result.getLastname());
        assertEquals("Manager", result.getTitle());
        assertEquals("Active", result.getStatus());
        assertEquals("New Address", result.getAddress());
        verify(contactRepo).save(existing);
    }

    @Test
    void updateContact_replacesEmailsAndPhones_withNewOnes() {
        Contact existing = new Contact();
        existing.setId("abc-123");
        existing.setEmails(new ArrayList<>(List.of(labeledEmail("Personal", "old@example.com"))));
        existing.setPhones(new ArrayList<>(List.of(labeledPhone("Home", "00000"))));

        Contact updatedData = new Contact();
        updatedData.setEmails(List.of(
                labeledEmail("Work", "work@example.com"),
                labeledEmail("Personal", "personal@example.com")
        ));
        updatedData.setPhones(List.of(labeledPhone("Work", "99999")));

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("abc-123", 1)).thenReturn(Optional.of(existing));
        when(contactRepo.save(existing)).thenReturn(existing);

        Contact result = contactService.updateContact("abc-123", updatedData, "testuser@example.com");

        assertEquals(2, result.getEmails().size());
        assertEquals("work@example.com", result.getEmails().get(0).getEmail());
        assertEquals("personal@example.com", result.getEmails().get(1).getEmail());
        assertEquals(1, result.getPhones().size());
        assertEquals("99999", result.getPhones().get(0).getPhone());

        // the old email/phone must be gone, not just appended to
        assertFalse(result.getEmails().stream().anyMatch(e -> e.getEmail().equals("old@example.com")));
        assertFalse(result.getPhones().stream().anyMatch(p -> p.getPhone().equals("00000")));
    }

    @Test
    void updateContact_clearsEmailsAndPhones_whenUpdatedDataHasNone() {
        Contact existing = new Contact();
        existing.setId("abc-123");
        existing.setEmails(new ArrayList<>(List.of(labeledEmail("Personal", "old@example.com"))));
        existing.setPhones(new ArrayList<>(List.of(labeledPhone("Home", "00000"))));

        Contact updatedData = new Contact(); // no emails/phones set - defaults to empty lists

        when(userRepository.findByEmailOrPhone("testuser@example.com")).thenReturn(testUser);
        when(contactRepo.findByIdAndOwnerId("abc-123", 1)).thenReturn(Optional.of(existing));
        when(contactRepo.save(existing)).thenReturn(existing);

        Contact result = contactService.updateContact("abc-123", updatedData, "testuser@example.com");

        assertTrue(result.getEmails().isEmpty());
        assertTrue(result.getPhones().isEmpty());
    }

    // ---------- uploadPhoto ----------

    @Test
    void uploadPhoto_throwsException_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/png", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> contactService.uploadPhoto("abc-123", "testuser@example.com", emptyFile));
    }

    @Test
    void uploadPhoto_throwsException_whenFileTooLarge() {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB, exceeds 5MB limit
        MockMultipartFile bigFile = new MockMultipartFile("file", "photo.png", "image/png", bigContent);

        assertThrows(IllegalArgumentException.class,
                () -> contactService.uploadPhoto("abc-123", "testuser@example.com", bigFile));
    }

    @Test
    void uploadPhoto_throwsException_whenContentTypeNotAllowed() {
        MockMultipartFile badFile = new MockMultipartFile("file", "photo.gif", "image/gif", new byte[]{1, 2, 3});

        assertThrows(IllegalArgumentException.class,
                () -> contactService.uploadPhoto("abc-123", "testuser@example.com", badFile));
    }
}
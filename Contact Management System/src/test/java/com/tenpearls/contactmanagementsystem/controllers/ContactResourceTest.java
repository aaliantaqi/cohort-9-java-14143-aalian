package com.tenpearls.contactmanagementsystem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpearls.contactmanagementsystem.domain.Contact;
import com.tenpearls.contactmanagementsystem.security.SecurityConfig;
import com.tenpearls.contactmanagementsystem.services.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactResource.class)
@Import(SecurityConfig.class)
class ContactResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // ---------- POST /api/contacts ----------

    @Test
    @WithMockUser(username = "testuser")
    void createContact_returns201_whenSuccessful() throws Exception {
        Contact newContact = new Contact();
        newContact.setFirstname("John");
        newContact.setLastname("Doe");

        Contact savedContact = new Contact();
        savedContact.setId("abc-123");
        savedContact.setFirstname("John");
        savedContact.setLastname("Doe");

        when(contactService.createContact(any(Contact.class), eq("testuser"))).thenReturn(savedContact);

        mockMvc.perform(post("/api/contacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc-123"));
    }

    // ---------- GET /api/contacts ----------

    @Test
    @WithMockUser(username = "testuser")
    void getContacts_returns200_withPagedResults() throws Exception {
        Contact contact = new Contact();
        contact.setId("abc-123");
        Page<Contact> page = new PageImpl<>(List.of(contact));

        when(contactService.getAllContacts(eq("testuser"), eq(0), eq(10), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("abc-123"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getContacts_passesSearchParam_whenProvided() throws Exception {
        Page<Contact> page = new PageImpl<>(List.of());

        when(contactService.getAllContacts(eq("testuser"), eq(0), eq(10), eq("john"))).thenReturn(page);

        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "john"))
                .andExpect(status().isOk());

        verify(contactService).getAllContacts("testuser", 0, 10, "john");
    }

    // ---------- GET /api/contacts/{id} ----------

    @Test
    @WithMockUser(username = "testuser")
    void getContact_returns200_whenFound() throws Exception {
        Contact contact = new Contact();
        contact.setId("abc-123");
        contact.setFirstname("John");
        contact.setLastname("Doe");

        when(contactService.getContact("abc-123", "testuser")).thenReturn(contact);

        mockMvc.perform(get("/api/contacts/abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    // ---------- PUT /api/contacts/photo ----------

    @Test
    @WithMockUser(username = "testuser")
    void uploadPhoto_returns200_whenSuccessful() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[]{1, 2, 3});

        when(contactService.uploadPhoto(eq("abc-123"), eq("testuser"), any()))
                .thenReturn("/api/contacts/abc-123/image");

        mockMvc.perform(multipart("/api/contacts/photo")
                        .file(file)
                        .param("id", "abc-123")
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("/api/contacts/abc-123/image"));
    }

    // ---------- DELETE /api/contacts/{id} ----------

    @Test
    @WithMockUser(username = "testuser")
    void deleteContact_returns204_whenSuccessful() throws Exception {
        doNothing().when(contactService).deleteContact("abc-123", "testuser");

        mockMvc.perform(delete("/api/contacts/abc-123").with(csrf()))
                .andExpect(status().isNoContent());

        verify(contactService).deleteContact("abc-123", "testuser");
    }

    // ---------- PUT /api/contacts/{id} ----------

    @Test
    @WithMockUser(username = "testuser")
    void updateContact_returns200_whenSuccessful() throws Exception {
        Contact updatedData = new Contact();
        updatedData.setFirstname("Updated");
        updatedData.setLastname("Name");

        Contact updatedContact = new Contact();
        updatedContact.setId("abc-123");
        updatedContact.setFirstname("Updated");
        updatedContact.setLastname("Name");

        when(contactService.updateContact(eq("abc-123"), any(Contact.class), eq("testuser")))
                .thenReturn(updatedContact);

        mockMvc.perform(put("/api/contacts/abc-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Updated"))
                .andExpect(jsonPath("$.lastname").value("Name"));
    }

    // ---------- GET /api/contacts/{id}/image ----------

    @Test
    @WithMockUser(username = "testuser")
    void getPhotoByContactId_returns404_whenContactNotFound() throws Exception {
        ContactService.ContactNotFoundException exception =
                contactService.new ContactNotFoundException("Contact Not Found!");

        when(contactService.getContact("missing-id", "testuser")).thenThrow(exception);

        mockMvc.perform(get("/api/contacts/missing-id/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getPhotoByContactId_returns404_whenPhotoUrlIsNull() throws Exception {
        Contact contact = new Contact();
        contact.setId("abc-123");
        contact.setPhotoUrl(null);

        when(contactService.getContact("abc-123", "testuser")).thenReturn(contact);

        mockMvc.perform(get("/api/contacts/abc-123/image"))
                .andExpect(status().isNotFound());
    }
}
package com.tenpearls.contactmanagementsystem.controllers;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import com.tenpearls.contactmanagementsystem.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.tenpearls.contactmanagementsystem.constant.Constant.PHOTO_DIRECTORY;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG_VALUE;
import static org.springframework.util.MimeTypeUtils.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactResource {
    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact, Authentication authentication) {
        Contact newContact = contactService.createContact(contact, authentication.getName());
        return ResponseEntity.created(URI.create("/contacts/" + newContact.getId())).body(newContact);
    }

    @GetMapping
    public ResponseEntity<Page<Contact>> getContacts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                                     Authentication authentication){
        return ResponseEntity.ok().body(contactService.getAllContacts(authentication.getName(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable("id") String id, Authentication authentication) {
        try {
            return ResponseEntity.ok().body(contactService.getContact(id, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("id") String id,
                                              @RequestParam("file") MultipartFile file,
                                              Authentication authentication) {
        return ResponseEntity.ok().body(contactService.uploadPhoto(id, authentication.getName(), file));
    }

    @GetMapping(path = "/image/{filename}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
        Path filePath = fileStorageLocation.resolve(filename).normalize();

        if (!filePath.startsWith(fileStorageLocation)) {
            throw new SecurityException("Invalid file path");
        }

        return Files.readAllBytes(filePath);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable("id") String id, Authentication authentication) {
        try {
            contactService.deleteContact(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
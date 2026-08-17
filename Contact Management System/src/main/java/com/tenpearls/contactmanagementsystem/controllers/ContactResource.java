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
import java.util.stream.Stream;

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
    public ResponseEntity<?> getContacts(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                         @RequestParam(value = "search", required = false) String search,
                                         Authentication authentication){
        return ResponseEntity.ok().body(contactService.getAllContacts(authentication.getName(), page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable("id") String id, Authentication authentication) {
        return ResponseEntity.ok().body(contactService.getContact(id, authentication.getName()));
    }

    @PutMapping("/photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("id") String id,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication authentication) {
        String photoUrl = contactService.uploadPhoto(id, authentication.getName(), file);
        return ResponseEntity.ok().body(photoUrl);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable("id") String id, Authentication authentication) {
        contactService.deleteContact(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(@PathVariable("id") String id,
                                                 @RequestBody Contact contact,
                                                 Authentication authentication) {
        Contact updated = contactService.updateContact(id, contact, authentication.getName());
        return ResponseEntity.ok(updated);
    }



    @GetMapping(path = "/{id}/image", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public ResponseEntity<byte[]> getPhotoByContactId(@PathVariable("id") String id, Authentication authentication) throws IOException {
        Contact contact;
        try {
            contact = contactService.getContact(id, authentication.getName());
        } catch (ContactService.ContactNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        if (contact.getPhotoUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();

        Path filePath;
        try (Stream<Path> files = Files.list(fileStorageLocation)) {
            filePath = files
                    .filter(p -> p.getFileName().toString().startsWith(id + "."))
                    .findFirst()
                    .orElse(null);
        }

        if (filePath == null || !filePath.normalize().startsWith(fileStorageLocation)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = filePath.toString().toLowerCase().endsWith(".png")
                ? IMAGE_PNG_VALUE
                : IMAGE_JPEG_VALUE;

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(Files.readAllBytes(filePath));
    }

}
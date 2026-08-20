package com.tenpearls.contactmanagementsystem.services;

import com.tenpearls.contactmanagementsystem.domain.Contact;
import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.repositories.ContactRepo;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.tenpearls.contactmanagementsystem.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor

public class ContactService {
    private final ContactRepo contactRepo;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

    public class ContactNotFoundException extends RuntimeException {
        public ContactNotFoundException(String message) {
            super(message);
        }
    }

    public Page<Contact> getAllContacts(String username, int page, int size, String search) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        User owner = getOwner(username);

        if (search != null && !search.isBlank()) {
            return contactRepo.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), search.trim(), PageRequest.of(page, size, Sort.by("name")));
        }
        return contactRepo.findByOwnerId(owner.getId(), PageRequest.of(page, size, Sort.by("name")));
    }


    public Contact getContact(String id, String username) {
        User owner = getOwner(username);
        return contactRepo.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ContactNotFoundException("Contact Not Found!"));
    }

    public Contact createContact(Contact contact, String username) {
        User owner = getOwner(username);
        contact.setOwner(owner);
        Contact saved = contactRepo.save(contact);
        log.info("New contact created (id={})", saved.getId());
        return saved;
    }


    public String uploadPhoto(String id, String username, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (5MB)");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }

        log.info("Saving Picture for contact ID: {}", id);
        Contact contact = getContact(id, username);
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoUrl(photoUrl);
        contactRepo.save(contact);
        return photoUrl;
    }

    public void deleteContact(String id, String username) {
        Contact contact = getContact(id, username);
        contactRepo.delete(contact);
        log.info("Contact deleted (id={})", id);
    }

    private User getOwner(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    private final Function<String, String> fileExtension = filename -> Optional.ofNullable(filename)
            .filter(name -> name.contains("."))
            .map(name -> "." + name.substring(name.lastIndexOf(".") + 1))
            .orElse(".png");

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            Path tempFile = fileStorageLocation.resolve(filename + ".tmp");
            Files.copy(image.getInputStream(), tempFile, REPLACE_EXISTING);

            try (Stream<Path> existing = Files.list(fileStorageLocation)) {
                existing.filter(p -> p.getFileName().toString().startsWith(id + ".") && !p.equals(tempFile))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                log.warn("Could not delete old photo file: {}", p, e);
                            }
                        });
            }

            Path finalPath = fileStorageLocation.resolve(filename);
            Files.move(tempFile, finalPath, REPLACE_EXISTING);

            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/contacts/" + id + "/image")
                    .toUriString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to Save Image", e);
        }
    };

    public Contact updateContact(String id, Contact updatedData, String username) {
        Contact existingContact = getContact(id, username);

        existingContact.setName(updatedData.getName());
        existingContact.setPhone(updatedData.getPhone());
        existingContact.setEmail(updatedData.getEmail());
        existingContact.setTitle(updatedData.getTitle());
        existingContact.setStatus(updatedData.getStatus());
        existingContact.setAddress(updatedData.getAddress());

        return contactRepo.save(existingContact);
    }

}
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.tenpearls.contactmanagementsystem.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepo contactRepo;
    private final UserRepository userRepository;

    public Page<Contact> getAllContacts(String username, int page, int size) {
        User owner = getOwner(username);
        return contactRepo.findByOwnerId(owner.getId(), PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id, String username) {
        User owner = getOwner(username);
        return contactRepo.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new RuntimeException("Contact Not Found!"));
    }

    public Contact createContact(Contact contact, String username) {
        User owner = getOwner(username);
        contact.setOwner(owner);
        return contactRepo.save(contact);
    }

    public String uploadPhoto(String id, String username, MultipartFile file) {
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
    }

    private User getOwner(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    private final Function<String, String> fileExtension = filename -> Optional.of(filename).filter(name -> name.contains("."))
            .map(name  -> "." + name.substring(filename.lastIndexOf(".") + 1)).orElse(".png");

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try{
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + filename)
                    .toUriString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to Save Image", e);
        }
    };
}
package com.tenpearls.contactmanagementsystem.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tenpearls.contactmanagementsystem.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonInclude(NON_DEFAULT)
@Table(name = "contacts")

public class Contact {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Id
    @UuidGenerator
    @Column(name = "id", unique = true, updatable = false, length = 36)
    private String id;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    private String photoUrl;
    private String title;
    private String status;
    private String address;

    @ElementCollection
    @CollectionTable(name = "contact_emails", joinColumns = @JoinColumn(name = "contact_id"))
    private List<LabeledEmail> emails = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "contact_phones", joinColumns = @JoinColumn(name = "contact_id"))
    private List<LabeledPhone> phones = new ArrayList<>();

    // Small nested classes instead of separate files.
    // @Embeddable just means "this is a group of columns that belongs to another table's row" -
    // it has no id of its own, and no other entity points to it.

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabeledEmail {
        private String label; // e.g. "Work", "Personal" - free text
        private String email;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabeledPhone {
        private String label; // e.g. "Work", "Home" - free text
        private String phone;
    }
}
package com.tenpearls.contactmanagementsystem.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(NON_DEFAULT)
@Table(name = "contacts")

public class Contact {
    @Id
    @UuidGenerator()
    @Column(name = "id", unique = true ,updatable = false)
    private String id;
    private String name;
    private String address; // Prefer to have a class
    private String email;
    private String phone;
    private String status; // It should be enum
    private String title;
    private String photoUrl;
}

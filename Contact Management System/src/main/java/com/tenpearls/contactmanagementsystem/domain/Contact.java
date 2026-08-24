package com.tenpearls.contactmanagementsystem.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tenpearls.contactmanagementsystem.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

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
    private String name;
    private String phone;
    private String email;
    private String photoUrl;
    private String title;
    private String status;
    private String address;
}

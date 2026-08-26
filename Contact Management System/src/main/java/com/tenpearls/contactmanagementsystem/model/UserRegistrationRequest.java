package com.tenpearls.contactmanagementsystem.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "password")
public class UserRegistrationRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String password;
}
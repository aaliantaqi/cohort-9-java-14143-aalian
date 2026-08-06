package com.tenpearls.contactmanagementsystem.model;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String firstname;
    private String lastname;
    private String username;
    private String password;
}
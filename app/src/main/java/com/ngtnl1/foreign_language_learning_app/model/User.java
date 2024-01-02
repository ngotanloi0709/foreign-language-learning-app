package com.ngtnl1.foreign_language_learning_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String dateOfBirth;

    public User(String email) {
        this.email = email;
    }

    public User(String email, String name, String phone, String dateOfBirth) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
    }
}

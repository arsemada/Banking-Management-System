package com.example.bankingmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Add this if you want a constructor with all fields

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter // Lombok will generate getters
@Setter // Lombok will generate setters
@NoArgsConstructor // Lombok will generate a no-argument constructor for JPA
@AllArgsConstructor // Lombok will generate a constructor with all fields
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password; // This should be encoded password

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    // Custom constructor for creation (if needed, otherwise @AllArgsConstructor suffices)
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
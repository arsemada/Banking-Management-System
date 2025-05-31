package com.example.bankingmanagement.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Import for handling bidirectional relationships
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data; // Using @Data combines @Getter, @Setter, @ToString, @EqualsAndHashCode
import lombok.NoArgsConstructor;

import java.util.ArrayList; // For accounts list
import java.util.HashSet;
import java.util.List; // For accounts list
import java.util.Set;

@Data // Combines @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // For easier object creation with .builder()
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // Add validation
    @Size(max = 20) // Add validation
    private String username;

    @NotBlank // Add validation
    @Size(max = 50) // Add validation
    @Email // Add validation
    private String email;

    @NotBlank // Add validation
    @Size(max = 120) // Add validation
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>(); // This links to the Role entity

    // --- NEW: One-to-Many relationship with Accounts ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Prevents infinite recursion when User is serialized
    private List<Account> accounts = new ArrayList<>(); // Initialize to avoid NullPointerException

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // --- NEW: Helper methods for bidirectional relationship management ---
    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }
}
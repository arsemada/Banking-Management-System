package com.example.bankingmanagement.payload.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data; // Import this
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Add this
@NoArgsConstructor // Add this
@AllArgsConstructor // Add this
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    // IMPORTANT: If your AuthService.java refers to getRoles(), this must be `roles`.
    // If it refers to getRole(), this can be `role`. Ensure consistency.
    private Set<String> roles; // Assuming 'roles' to match common use in Spring Security tutorial examples

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    // Lombok will generate all getters and setters.
}
package com.example.bankingmanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // Generates constructor MessageResponse(String message)
public class MessageResponse {
    private String message;
}
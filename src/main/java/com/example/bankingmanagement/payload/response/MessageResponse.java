package com.example.bankingmanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // This generates the public MessageResponse(String message) constructor
public class MessageResponse {
    private String message;
}
package com.example.bankingmanagement.payload.response;

public class MessageResponse {
    private String message;

    public MessageResponse(String message) { // Make sure this constructor exists
        this.message = message;
    }

    public MessageResponse() { // Make sure this no-arg constructor exists
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
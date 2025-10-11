package com.team.codejam.dto;

public class AuthResponseDto {
    private String message;

    public AuthResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

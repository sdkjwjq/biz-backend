package org.example.service;

public class AdminManagementException extends RuntimeException {
    private final int code;

    public AdminManagementException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package com.example.mafiagame.constant;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("Member does not exist");
    }

    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

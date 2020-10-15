package ru.mikhailb.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {

    private int id;
    private String token;
    private String role;

    @Override
    public String toString() {
        return String.format("id: %d, token: %s, role: %s", id, token, role);
    }
}

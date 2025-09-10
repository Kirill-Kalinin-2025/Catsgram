package ru.yandex.practicum.catsgram.model;

import java.time.Instant;
import lombok.Data;

@Data
public class User {
    Long id;
    String username;
    String email;
    String password;
    Instant registrationDate;
}
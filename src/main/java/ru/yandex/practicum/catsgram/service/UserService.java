package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }

        if (isEmailAlreadyUsed(user.getEmail())) {
            throw new DuplicatedDataException("Этот Email уже используется");
        }

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new ConditionsNotMetException("Пользователь с id " + user.getId() + " не найден");
        }

        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (isEmailAlreadyUsed(user.getEmail())) {
                throw new DuplicatedDataException("Этот Email уже используется");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getPassword() != null) {
            existingUser.setPassword(user.getPassword());
        }

        return existingUser;
    }

    private boolean isEmailAlreadyUsed(String email) {
        return users.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
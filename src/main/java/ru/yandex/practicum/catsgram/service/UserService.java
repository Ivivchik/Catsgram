package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public User create(User user)
            throws ConditionsNotMetException, DuplicatedDataException {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

        checkEmail(user.getEmail());

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        String newName = newUser.getUsername();
        String newPassword = newUser.getPassword();
        String newEmail = newUser.getEmail();

        if (newName != null) {
            oldUser.setUsername(newName);
        }
        if (newPassword != null) {
            oldUser.setPassword(newPassword);
        }
        if (newEmail != null && checkEmail(newEmail)) {
            oldUser.setEmail(newEmail);
        }
        return oldUser;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean checkEmail(String email) throws DuplicatedDataException {
        Optional<String> emailOpt = users.values().stream()
                .map(User::getEmail)
                .filter(e -> e.equals(email))
                .findFirst();
        if (emailOpt.isPresent()) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        return true;
    }
}

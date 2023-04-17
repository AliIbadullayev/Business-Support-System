package org.billing.crm.services;

import org.billing.crm.exception.NotFoundUserException;
import org.billing.data.models.User;
import org.billing.data.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String username){
        Optional<User> user = userRepository.findById(username);
        if (user.isEmpty())
            throw new NotFoundUserException("Пользователь с данным username не найден!");
        return user.get();
    }
}

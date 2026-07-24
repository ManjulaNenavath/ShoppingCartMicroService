package com.ecommerce.user.service;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.exception.DuplicateResourceException;
import com.ecommerce.user.exception.InvalidCredentialsException;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Business logic for users. Controllers stay thin; all rules live here.
 */
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponse register(UserRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("username already taken: " + request.username());
        }
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email already registered: " + request.email());
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password()); // NOTE: plaintext for the demo only - never do this in prod.
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        return UserResponse.from(repository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        User user = repository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("invalid username or password"));
        if (!user.getPassword().equals(request.password())) {
            throw new InvalidCredentialsException("invalid username or password");
        }
        // Demo token only (base64 of "username:id:timestamp"). Not a signed JWT.
        String raw = user.getUsername() + ":" + user.getId() + ":" + System.currentTimeMillis();
        String token = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return new LoginResponse(token, user.getId(), user.getUsername(), "login successful");
    }

    public UserResponse getById(Long id) {
        return repository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with id: " + id));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with id: " + id));
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        return UserResponse.from(repository.save(user));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("user not found with id: " + id);
        }
        repository.deleteById(id);
    }
}

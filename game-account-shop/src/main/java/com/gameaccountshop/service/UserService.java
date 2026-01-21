package com.gameaccountshop.service;

import com.gameaccountshop.dto.UserDTO;
import com.gameaccountshop.dto.UserRegistrationRequest;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.exception.BusinessException;
import com.gameaccountshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with the provided registration request
     * @param request UserRegistrationRequest with username, password, email
     * @return UserDTO of the created user
     * @throws BusinessException if username already exists
     */
    public UserDTO registerUser(UserRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã được sử dụng!");
        }

        // Create new user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);

        // Save to database
        User savedUser = userRepository.save(user);

        // Convert and return as DTO
        return toDTO(savedUser);
    }

    /**
     * Convert User entity to UserDTO
     * Prevents entity exposure to API layer
     */
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

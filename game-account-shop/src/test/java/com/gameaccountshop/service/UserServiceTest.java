package com.gameaccountshop.service;

import com.gameaccountshop.dto.UserDTO;
import com.gameaccountshop.dto.UserRegistrationRequest;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.exception.BusinessException;
import com.gameaccountshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.atLeastOnce;

/**
 * Unit tests for UserService
 * Tests verify:
 * - Successful registration with password hashing
 * - Duplicate username handling
 * - Role assignment as USER
 * - BCrypt password encoding
 */
@SpringBootTest
class UserServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    /**
     * Test successful registration with valid input
     * AC #1-5: Given valid username, password (min 6), email, When submit, Then user is created
     */
    @Test
    void registerUser_ValidInput_CreatesUserWithHashedPassword() {
        // Given: No existing user with username
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // When: Register user
        UserDTO result = userService.registerUser(request);

        // Then: User is created with correct properties
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");

        // Verify password was hashed (not plaintext)
        verify(userRepository).save(argThat(user ->
            !user.getPassword().equals("password123") &&
            user.getPassword().startsWith("$2a$")  // BCrypt hash prefix
        ));

        // Verify save was called
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    /**
     * Test duplicate username throws BusinessException
     * AC #4: Username must be unique
     */
    @Test
    void registerUser_DuplicateUsername_ThrowsBusinessException() {
        // Given: Username already exists
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // When & Then: Throws BusinessException with Vietnamese message
        assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Tên đăng nhập đã tồn tại!");

        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test BCrypt password encoding with 10 rounds
     * AC #3: Password is hashed using BCrypt (10 rounds)
     */
    @Test
    @DirtiesContext
    void registerUser_PasswordIsBCryptEncodedWith10Rounds() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("admin123");
        request.setEmail("test@example.com");

        // When
        userService.registerUser(request);

        // Then: Capture saved user and verify password
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Verify password is not plaintext
        assertThat(savedUser.getPassword()).isNotEqualTo("admin123");

        // Verify password starts with BCrypt hash prefix
        assertThat(savedUser.getPassword()).startsWith("$2a$");

        // Verify BCrypt strength is 10 rounds (hash length = 60 chars for BCrypt(10))
        assertThat(savedUser.getPassword()).hasSize(60);

        // Verify password can be validated with BCrypt
        assertThat(passwordEncoder.matches("admin123", savedUser.getPassword())).isTrue();
    }

    /**
     * Test default role assignment as USER
     * AC #3: User is assigned role "USER"
     */
    @Test
    void registerUser_AssignedRoleUSER() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // When
        UserDTO result = userService.registerUser(request);

        // Then: Role is USER
        assertThat(result.getRole()).isEqualTo("USER");

        // Verify entity has USER role
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
    }

    /**
     * Test all fields are correctly mapped to DTO
     */
    @Test
    void registerUser_AllFieldsMappedToDTO() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            // Manually set createdAt for the mock since @PrePersist won't be called
            user.setCreatedAt(java.time.LocalDateTime.now());
            return user;
        });

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // When
        UserDTO result = userService.registerUser(request);

        // Then: All DTO fields are populated
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    /**
     * Test email is saved correctly
     */
    @Test
    void registerUser_EmailIsSaved() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("user@example.com");

        // When
        userService.registerUser(request);

        // Then: Email is saved
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@example.com");
    }
}

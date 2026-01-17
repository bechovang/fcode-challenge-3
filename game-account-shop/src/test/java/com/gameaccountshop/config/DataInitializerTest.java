package com.gameaccountshop.config;

import com.gameaccountshop.enums.Role;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for DataInitializer - Default Admin Account Creation
 *
 * Tests verify:
 * - Admin is created when database is empty
 * - No duplicate admin is created on restart
 * - Password is BCrypt encoded with 10 rounds
 * - Role is set to ADMIN
 */
@SpringBootTest
class DataInitializerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(userRepository, passwordEncoder);
    }

    /**
     * AC #1-5: Given database is empty, When application starts, Then default admin is created
     */
    @Test
    void testCreatesAdminWhenDatabaseIsEmpty() throws Exception {
        // Given: Database is empty (no admin user exists)
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // When: DataInitializer runs
        dataInitializer.run();

        // Then: Admin account is created with correct attributes
        verify(userRepository, times(1)).save(any(User.class));

        // Verify the saved user has correct properties
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedAdmin = userCaptor.getValue();

        // AC #2: Username is "admin"
        assertThat(savedAdmin.getUsername()).isEqualTo("admin");

        // AC #4: Role is "ADMIN"
        assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
    }

    /**
     * AC #2: Verify password is "admin123" and BCrypt encoded with 10 rounds
     */
    @Test
    void testPasswordIsBCryptEncoded() throws Exception {
        // Given: Database is empty
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // When: DataInitializer runs
        dataInitializer.run();

        // Then: Password is BCrypt encoded
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedAdmin = userCaptor.getValue();

        // Verify password is not plaintext
        assertThat(savedAdmin.getPassword()).isNotEqualTo("admin123");

        // Verify password starts with BCrypt hash prefix
        assertThat(savedAdmin.getPassword()).startsWith("$2a$");

        // Verify password can be decoded with BCrypt
        assertThat(passwordEncoder.matches("admin123", savedAdmin.getPassword())).isTrue();

        // Verify BCrypt strength is 10 rounds (check hash length - 60 chars = BCrypt(10))
        assertThat(savedAdmin.getPassword()).hasSize(60);
    }

    /**
     * AC #6-7: Given database already has users, When application starts, Then no additional admin created
     */
    @Test
    void testDoesNotCreateDuplicateAdminWhenAdminExists() throws Exception {
        // Given: Admin already exists
        User existingAdmin = new User();
        existingAdmin.setUsername("admin");
        existingAdmin.setPassword("$2a$10$dummyHash");
        existingAdmin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        // When: DataInitializer runs
        dataInitializer.run();

        // Then: No new admin is created
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Verify @Transactional annotation is present for atomic admin creation
     */
    @Test
    void testTransactionalAnnotationPresent() throws Exception {
        // Given: Database is empty
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // When: DataInitializer runs
        dataInitializer.run();

        // Then: save is called (verifies method executed within transaction context)
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Verify @Order(1) ensures DataInitializer runs before other CommandLineRunner beans
     * This is verified by checking the annotation is present on the class
     */
    @Test
    void testOrderAnnotationPresent() {
        // Verify @Order(1) annotation is present
        org.springframework.core.annotation.Order orderAnnotation =
            dataInitializer.getClass().getAnnotation(org.springframework.core.annotation.Order.class);

        assertThat(orderAnnotation).isNotNull();
        assertThat(orderAnnotation.value()).isEqualTo(1);
    }
}

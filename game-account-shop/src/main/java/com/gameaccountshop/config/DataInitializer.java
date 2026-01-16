package com.gameaccountshop.config;

import com.gameaccountshop.entity.User;
import com.gameaccountshop.enums.Role;
import com.gameaccountshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only create admin if no users exist
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@gameaccountshop.com");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            log.info("=================================================================");
            log.info("DEFAULT ADMIN ACCOUNT CREATED");
            log.info("Username: admin");
            log.info("Password: admin123");
            log.info("=================================================================");
        } else {
            log.info("Users already exist - skipping default admin creation");
        }
    }
}

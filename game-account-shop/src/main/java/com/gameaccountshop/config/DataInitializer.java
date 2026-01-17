package com.gameaccountshop.config;

import com.gameaccountshop.enums.Role;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1) // Chạy đầu tiên, trước bất kỳ CommandLineRunner nào khác
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
        // Kiểm tra xem admin đã tồn tại chưa
        if (userRepository.findByUsername("admin").isEmpty()) {

            // Tạo admin mặc định
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            // Ghi log thông báo thành công
            log.warn("=================================================================");
            log.warn("DA TAO TAI KHOAN ADMIN MAC DINH");
            log.warn("Username: admin");
            log.warn("Password: admin123");
            log.warn("=================================================================");
            log.warn("QUAN TRONG: Vui Long Doi Mat Khau Sau Khi Dang Nhap!");
            log.warn("=================================================================");

        } else {
            log.info("tai khoan admin da ton tai!");
        }
    }
}
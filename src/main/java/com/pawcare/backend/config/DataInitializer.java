package com.pawcare.backend.config;

import com.pawcare.backend.entity.Role;
import com.pawcare.backend.entity.User;
import com.pawcare.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            String adminEmail = "admin@pawcare.com";

            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = new User();

                admin.setFullName("PawCare Admin");
                admin.setEmail(adminEmail);
                admin.setPhone("0770000000");
                admin.setAddress("PawCare");
                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println(
                        "PawCare admin account created successfully."
                );
            }
        };
    }
}
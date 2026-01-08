package dev.jsmitty.bux.system.config;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.repository.AdminRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds default data for local/dev environments.
 *
 * <p>Creates a super admin on startup for non-test profiles.
 */
@Configuration
public class DataInitializer {

    @Bean
    @Profile("!test")
    CommandLineRunner initData(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Admin admin =
                    new Admin("admin", passwordEncoder.encode("password"), "admin@example.com");
            admin.setSuperAdmin(true);
            adminRepository.save(admin);

            System.out.println("Login with: admin / password");
        };
    }
}

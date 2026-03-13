package com.permguard;

import com.permguard.entity.User;
import com.permguard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

// ================================================================
//  DataSeeder — inserts test users on first startup
//
//  HOW TO USE:
//  1. Add this file to your project
//  2. Add "spring.profiles.active=dev" to application.properties
//  3. Start the app ONCE — users will be inserted
//  4. Remove "spring.profiles.active=dev" (or delete this file)
//
//  Test credentials after seeding:
//    admin@permguard.com    / Admin@1234
//    faculty@permguard.com  / Faculty@1234
//    student@permguard.com  / Student@1234
//    security@permguard.com / Security@1234
// ================================================================

@Configuration
@Profile("dev")   // Only runs when spring.profiles.active=dev
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepo,
                                        PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepo.existsByEmail("admin@permguard.com")) {
                System.out.println("[Seeder] Users already exist, skipping seed.");
                return;
            }

            System.out.println("[Seeder] Inserting test users...");

            // ── ADMIN ─────────────────────────────────────────
            User admin = new User();
            admin.setFullName("System Admin");
            admin.setEmail("admin@permguard.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
            admin.setRole(User.Role.ADMIN);
            admin.setIsActive(true);
            admin.setAccountLocked(false);
            admin.setFailedAttempts(0);
            admin.setMustChangePassword(false);
            userRepo.save(admin);
            System.out.println("[Seeder] Created ADMIN: admin@permguard.com / Admin@1234");

            // ── FACULTY ───────────────────────────────────────
            User faculty = new User();
            faculty.setFullName("Mr. S. Naveen Kumar");
            faculty.setEmail("faculty@permguard.com");
            faculty.setPasswordHash(passwordEncoder.encode("Faculty@1234"));
            faculty.setRole(User.Role.FACULTY);
            faculty.setIsActive(true);
            faculty.setAccountLocked(false);
            faculty.setFailedAttempts(0);
            faculty.setMustChangePassword(false);
            userRepo.save(faculty);
            System.out.println("[Seeder] Created FACULTY: faculty@permguard.com / Faculty@1234");

            // ── STUDENT ───────────────────────────────────────
            User student = new User();
            student.setRollNumber("2211CS010271");
            student.setFullName("K. Mallikarjunarao");
            student.setEmail("student@permguard.com");
            student.setPasswordHash(passwordEncoder.encode("Student@1234"));
            student.setRole(User.Role.STUDENT);
            student.setYearOfStudy(3);
            student.setIsActive(true);
            student.setAccountLocked(false);
            student.setFailedAttempts(0);
            student.setMustChangePassword(false);
            userRepo.save(student);
            System.out.println("[Seeder] Created STUDENT: student@permguard.com / Student@1234");

            // ── SECURITY ──────────────────────────────────────
            User security = new User();
            security.setFullName("Gate Security");
            security.setEmail("security@permguard.com");
            security.setPasswordHash(passwordEncoder.encode("Security@1234"));
            security.setRole(User.Role.SECURITY);
            security.setIsActive(true);
            security.setAccountLocked(false);
            security.setFailedAttempts(0);
            security.setMustChangePassword(false);
            userRepo.save(security);
            System.out.println("[Seeder] Created SECURITY: security@permguard.com / Security@1234");

            System.out.println("[Seeder] ✅ All test users created successfully!");
        };
    }
}
package com.permguard.repository;

import com.permguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);

    // ✅ Auto-assign faculty
    Optional<User> findFirstByRole(User.Role role);
    boolean existsByRollNumber(String rollNumber);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);

    @Query("SELECT u.userId FROM User u WHERE u.email = :email")
    Optional<Long> findUserIdByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true WHERE u.email = :email")
    void lockAccount(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0 WHERE u.email = :email")
    void resetFailedAttempts(@Param("email") String email);
}
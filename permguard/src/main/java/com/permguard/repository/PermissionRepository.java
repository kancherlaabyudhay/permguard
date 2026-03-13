package com.permguard.repository;

import com.permguard.entity.Permission;
import com.permguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByStudentOrderByCreatedAtDesc(User student);
    List<Permission> findByFacultyAndStatusOrderByCreatedAtDesc(User faculty, Permission.Status status);
    List<Permission> findByStatusOrderByCreatedAtDesc(Permission.Status status);

    // ✅ Admin sees all permissions
    List<Permission> findAllByOrderByCreatedAtDesc();

    Optional<Permission> findByQrToken(String qrToken);
    long countByStudentAndStatus(User student, Permission.Status status);
    long countByStatus(Permission.Status status);

    List<Permission> findByStatusAndExpiryTimeBefore(Permission.Status status, LocalDateTime time);

    // Used by PermissionExpiryScheduler
    @Query("SELECT p FROM Permission p WHERE p.status = 'APPROVED' AND p.expiryTime < :now")
    List<Permission> findExpiredApprovedPermissions(@Param("now") LocalDateTime now);

    // Analytics queries
    @Query("SELECT p.type, COUNT(p) FROM Permission p GROUP BY p.type ORDER BY COUNT(p) DESC")
    List<Object[]> countGroupByType();

    @Query("SELECT d.deptName, COUNT(p) FROM Permission p JOIN p.student s JOIN s.department d GROUP BY d.deptName ORDER BY COUNT(p) DESC")
    List<Object[]> countGroupByDepartment();

    @Query("SELECT FUNCTION('DATE', p.createdAt), COUNT(p) FROM Permission p WHERE p.createdAt >= :from GROUP BY FUNCTION('DATE', p.createdAt) ORDER BY FUNCTION('DATE', p.createdAt) ASC")
    List<Object[]> countGroupByDate(@Param("from") LocalDateTime from);

    @Query("SELECT s.fullName, s.rollNumber, COUNT(p) FROM Permission p JOIN p.student s GROUP BY s.userId, s.fullName, s.rollNumber ORDER BY COUNT(p) DESC")
    List<Object[]> topStudentsByPermissions();
}
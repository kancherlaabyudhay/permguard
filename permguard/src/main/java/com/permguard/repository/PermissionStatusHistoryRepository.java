package com.permguard.repository;

import com.permguard.entity.PermissionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionStatusHistoryRepository extends JpaRepository<PermissionStatusHistory, Long> {

    List<PermissionStatusHistory> findByPermission_IdOrderByChangedAtAsc(Long permissionId);
}
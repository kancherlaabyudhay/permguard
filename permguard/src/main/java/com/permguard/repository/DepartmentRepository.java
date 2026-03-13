
package com.permguard.repository;

import com.permguard.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByDeptCode(String deptCode);
    boolean existsByDeptCode(String deptCode);
}

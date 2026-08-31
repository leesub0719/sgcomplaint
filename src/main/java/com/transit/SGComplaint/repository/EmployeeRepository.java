package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeeRepository extends
        JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    boolean existsByEmpId(String empId);

    Optional<Employee> findByEmpId(String empId);

    Optional<Employee> findByEmpIdAndEmpStatus(String empId, String empStatus);

    long countByEmpStatus(String empStatus);

    long countByEmpRole(String empRole);
}

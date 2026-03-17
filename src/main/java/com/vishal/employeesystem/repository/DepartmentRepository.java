package com.vishal.employeesystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vishal.employeesystem.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Long> {

}

package com.vishal.employeesystem.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.vishal.employeesystem.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

	

}

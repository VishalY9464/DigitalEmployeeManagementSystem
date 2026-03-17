package com.vishal.employeesystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vishal.employeesystem.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}

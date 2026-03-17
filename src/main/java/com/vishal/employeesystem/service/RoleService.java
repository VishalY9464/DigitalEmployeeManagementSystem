package com.vishal.employeesystem.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vishal.employeesystem.entity.Role;
import com.vishal.employeesystem.repository.RoleRepository;

@Service
public class RoleService {
	
	@Autowired
	private RoleRepository roleRepository;
	
	public Role createRole(Role role) {
			return roleRepository.save(role);
	}
	
	public List<Role> getAllRoles(){
		return roleRepository.findAll();
	}

}

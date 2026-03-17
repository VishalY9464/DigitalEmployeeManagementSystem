package com.vishal.employeesystem.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vishal.employeesystem.dto.RegisterRequestDTO;
import com.vishal.employeesystem.entity.Role;
import com.vishal.employeesystem.entity.User;
import com.vishal.employeesystem.exception.ResourceNotFoundException;
import com.vishal.employeesystem.repository.RoleRepository;
import com.vishal.employeesystem.repository.UserRepository;

@Service
public class UserService  {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public User registerUser(RegisterRequestDTO dto) {  
    User user= new User();
	user.setUsername(dto.getUsername());
	user.setEmail(dto.getEmail());
	
	//encrypt the password
	user.setPassword(passwordEncoder.encode(dto.getPassword()));
		
		Set<Role> roles= new HashSet<>();
				
	    for(Long roleId: dto.getRoleIds()) {
	    	
	    	Role existingRole= roleRepository.findById(roleId)
	    			.orElseThrow(()-> new ResourceNotFoundException("Role not found"));
	    	
	    	roles.add(existingRole);
	    }
	    
	   user.setRoles(roles); 
	   return userRepository.save(user);
	}

}

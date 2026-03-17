package com.vishal.employeesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vishal.employeesystem.dto.RegisterRequestDTO;
import com.vishal.employeesystem.entity.User;
import com.vishal.employeesystem.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public User registerUser(@RequestBody RegisterRequestDTO dto) {
		return userService.registerUser(dto);
	}

}

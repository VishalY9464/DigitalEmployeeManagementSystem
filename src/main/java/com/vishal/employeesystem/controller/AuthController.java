package com.vishal.employeesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vishal.employeesystem.dto.LoginRequestDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping("/login")
	public String login( @Valid @RequestBody LoginRequestDTO request) {
		authenticationManager.authenticate(
             new UsernamePasswordAuthenticationToken(
            		 request.getUsername(),
            		 request.getPassword()));
		
		return "Login succesfull";
	}
}

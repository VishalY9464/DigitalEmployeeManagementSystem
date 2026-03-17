package com.vishal.employeesystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeRequestDTO {
	
	@NotBlank(message="Name is required ")
	@Size(min=2, max=50)
	private String name;
	
	@NotBlank(message="Email is reuqired ")
	@Email(message="Email should be valid ")
	private String email;
	
	@Positive(message="salary should be positive ")
	private Double salary;
	
    @NotNull(message = "Department ID is required ")
    @Positive(message = "Department ID must be a positive number ")
	private Long departmentId;
	

}

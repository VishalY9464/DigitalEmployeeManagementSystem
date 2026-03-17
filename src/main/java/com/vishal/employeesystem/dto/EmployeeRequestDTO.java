package com.vishal.employeesystem.dto;

import lombok.Data;

@Data
public class EmployeeRequestDTO {
	
	private String name;
	private String email;
	private Double salary;
	private Long departmentId;
	

}

package com.vishal.employeesystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vishal.employeesystem.dto.EmployeeRequestDTO;
import com.vishal.employeesystem.entity.Employee;
import com.vishal.employeesystem.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
	
	@Autowired
	EmployeeService employeeService;
	
	@PostMapping
	public ResponseEntity<Employee> addEmployee( @Valid @RequestBody EmployeeRequestDTO dto){
		Employee emp=employeeService.addEmployee(dto);
		return ResponseEntity.ok(emp);	
	}
	
	@GetMapping
	public ResponseEntity<List<Employee>>getAllEmployee(){
		List<Employee> emp=employeeService.getAllEmployee();
		return ResponseEntity.ok(emp);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id){
		Employee emp=employeeService.getEmployeeById(id);
		return ResponseEntity.ok(emp);
	}
	
	@PutMapping("/{id}/{salary}")
	public ResponseEntity<Employee> updateEmployeeById(@PathVariable Long id, @PathVariable double salary){
		Employee emp=employeeService.updateEmployee(id, salary);
		return ResponseEntity.ok(emp);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteEmployeeById(@PathVariable Long id){
		String result=employeeService.deleteEmployeeById(id);
		return ResponseEntity.ok(result);
	}
	
	

	
	
	

}

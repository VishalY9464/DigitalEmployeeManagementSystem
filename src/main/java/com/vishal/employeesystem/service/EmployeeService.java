package com.vishal.employeesystem.service;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vishal.employeesystem.dto.EmployeeRequestDTO;
import com.vishal.employeesystem.entity.Department;
import com.vishal.employeesystem.entity.Employee;
import com.vishal.employeesystem.exception.ResourceNotFoundException;
import com.vishal.employeesystem.repository.DepartmentRepository;
import com.vishal.employeesystem.repository.EmployeeRepository;

@Service
public class EmployeeService {
 
	@Autowired
	private EmployeeRepository employeeRepo;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	//add employee
	public Employee addEmployee(EmployeeRequestDTO dto) {

	   Department department=departmentRepository.findById(dto.getDepartmentId())
			   .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
	   Employee emp=new Employee();
	   
	   emp.setName(dto.getName());
	   emp.setEmail(dto.getEmail());
	   emp.setSalary(dto.getSalary());
	   emp.setDepartment(department);
	   
	   return employeeRepo.save(emp);

	}

	//getAllEmployee
	public List<Employee> getAllEmployee(){
		return employeeRepo.findAll();
	}
	
	//getEmployeeById
	public Employee getEmployeeById(Long id) {
		return employeeRepo.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Employee Not Found"));
	}
	
	//update employee by id
    public Employee updateEmployee(Long id,double newSalary) {
       Employee emp=getEmployeeById(id);
       if(emp==null) {
    	  return null;
       }
       emp.setSalary(newSalary);
       return emp;
    }
    
    //search employee by id;
    public Employee searchEmployeeById(Long id) {
    	Employee emp=getEmployeeById(id);
    	if(emp==null) {
    		return null;
    	}
    	return emp;
    }
    
   //delete employee by id
    public String deleteEmployeeById(Long id) {
       Employee emp=getEmployeeById(id);
       if(emp!=null) {
    	   employeeRepo.deleteById(id);
    	   return "Employee deleted successfully";
       }
       return null;
    }
	
	

	
}

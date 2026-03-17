package com.vishal.employeesystem.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vishal.employeesystem.entity.Department;
import com.vishal.employeesystem.repository.DepartmentRepository;

@Service
public class DepartmentService {
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	public Department createDepartment(Department department) {
		return departmentRepository.save(department);
	}
	
	public List<Department> getAllDepartment(){
		return departmentRepository.findAll();   
	}

}

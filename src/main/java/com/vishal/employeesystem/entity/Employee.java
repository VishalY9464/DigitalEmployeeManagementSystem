package com.vishal.employeesystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="employee",
uniqueConstraints = {
		   @UniqueConstraint(columnNames="email")
},
indexes= {
		   @Index(name="idx_email", columnList="email")
    }
	)
public class Employee {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	 private Long id;
	
	@NotNull(message="Name should not null")
	@Size(max=100)
	private String name;
	
	@Email
	@NotNull(message="Email should not null")
	private String email;
	
	@NotNull(message="salary should not null")
	@Positive(message="salary should be positive")
	private Double salary;
	
	@ManyToOne
	@JoinColumn(name = "department_id")
	private Department department;
	
	
	
}

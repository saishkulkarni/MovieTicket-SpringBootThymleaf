package com.saish.movie_ticket.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Component
@Entity
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Size(min = 3, max = 20, message = "* Enter between 3~20 charecters")
	private String name;
	@DecimalMin(value = "6000000000", message = "* Enter proper Mobile Number")
	@DecimalMax(value = "9999999999", message = "* Enter proper Mobile Number")
	private long mobile;
	@NotEmpty(message = "* It is Compulsory Field")
	@Email(message = "* Enter proper Email")
	private String email;
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	private String password;
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	@Transient
	private String confirmPassword;
	@NotNull(message = "* It is Compulsory Field")
	private String gender;
	@Past(message = "* Enter proper DOB")
	@NotNull(message = "* It is Compulsory Field")
	private LocalDate dob;
	private int otp;
	private boolean verified;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	List<Booking> bookingList = new ArrayList<Booking>();
}

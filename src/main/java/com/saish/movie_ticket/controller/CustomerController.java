package com.saish.movie_ticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.saish.movie_ticket.dto.Customer;
import com.saish.movie_ticket.helper.AES;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	Customer customer;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	TheatreRepository theatreRepository;

	@GetMapping("/signup")
	public String loadSignup(ModelMap map) {
		map.put("customer", customer);
		return "customer-signup.html";
	}

	@PostMapping("/signup")
	public String signup(@Valid Customer customer, BindingResult result) {
		if (!customer.getPassword().equals(customer.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword", "* Password Missmatch");
		}
		if (customerRepository.existsByEmail(customer.getEmail())
				|| theatreRepository.existsByEmail(customer.getEmail())) {
			result.rejectValue("email", "error.email", "* Account Already Exists");
		}
		if (customerRepository.existsByMobile(customer.getMobile())
				|| theatreRepository.existsByMobile(customer.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists");
		}

		if (result.hasErrors()) {
			return "customer-signup.html";
		} else {
			customer.setPassword(AES.encrypt(customer.getPassword(), "123"));
			return "home.html";
		}
	}
}

package com.saish.movie_ticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.saish.movie_ticket.dto.Theatre;
import com.saish.movie_ticket.helper.AES;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/theatre")
public class TheaterController {

	@Autowired
	Theatre theatre;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	TheatreRepository theatreRepository;

	@GetMapping("/signup")
	public String loadSignup(ModelMap map) {
		map.put("theatre", theatre);
		return "theatre-signup.html";
	}

	@PostMapping("/signup")
	public String signup(@Valid Theatre theatre, BindingResult result) {
		if (!theatre.getPassword().equals(theatre.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword", "* Password Missmatch");
		}
		if (customerRepository.existsByEmail(theatre.getEmail())
				|| theatreRepository.existsByEmail(theatre.getEmail())) {
			result.rejectValue("email", "error.email", "* Account Already Exists");
		}
		if (customerRepository.existsByMobile(theatre.getMobile())
				|| theatreRepository.existsByMobile(theatre.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists");
		}

		if (result.hasErrors()) {
			return "theatre-signup.html";
		} else {
			theatre.setPassword(AES.encrypt(theatre.getPassword(), "123"));
			return "home.html";
		}
	}

}

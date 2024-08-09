package com.saish.movie_ticket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.saish.movie_ticket.dto.Theatre;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

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
	
	

}

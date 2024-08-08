package com.saish.movie_ticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/theatre")
public class TheaterController {

	@GetMapping("/signup")
	public String loadSignup() {
		return "theatre-signup.html";
	}

}

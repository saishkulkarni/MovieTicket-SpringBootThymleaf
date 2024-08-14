package com.saish.movie_ticket.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MyErrorController implements ErrorController {
	@RequestMapping("/error")
	public String errorHandling(HttpServletRequest request) {
		int errorCode = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (errorCode == 404)
			return "404.html";
		else
			return "500.html";
	}
}

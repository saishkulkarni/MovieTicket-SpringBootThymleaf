package com.saish.movie_ticket.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.saish.movie_ticket.dto.Customer;
import com.saish.movie_ticket.helper.AES;
import com.saish.movie_ticket.helper.EmailSendingHelper;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

import jakarta.servlet.http.HttpSession;
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

	@Autowired
	EmailSendingHelper emailSendingHelper;

	@GetMapping("/signup")
	public String loadSignup(ModelMap map) {
		map.put("customer", customer);
		return "customer-signup.html";
	}

	@PostMapping("/signup")
	public String signup(@Valid Customer customer, BindingResult result, HttpSession session) {
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
			customer.setOtp(new Random().nextInt(100000, 1000000));
			System.out.println("OTP - > " + customer.getOtp());
			emailSendingHelper.sendMailToCustomer(customer);
			customerRepository.save(customer);
			session.setAttribute("success", "Otp Sent Success!!!");
			session.setAttribute("id", customer.getId());
			return "redirect:/customer/enter-otp";
		}
	}

	@GetMapping("/resend-otp/{id}")
	public String resendOtp(@PathVariable int id, HttpSession session) {
		Customer customer = customerRepository.findById(id).get();
		customer.setOtp(new Random().nextInt(100000, 1000000));
		emailSendingHelper.sendMailToCustomer(customer);
		customerRepository.save(customer);
		session.setAttribute("success", "Otp Resent Success!!!");
		session.setAttribute("id", customer.getId());
		return "redirect:/customer/enter-otp";
	}

	@GetMapping("/enter-otp")
	public String enterOtp(ModelMap map) {
		map.put("user", "customer");
		return "enter-otp.html";
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam int id, @RequestParam int otp, HttpSession session) {
		Customer customer = customerRepository.findById(id).orElseThrow();
		if (customer.getOtp() == otp) {
			customer.setVerified(true);
			customerRepository.save(customer);
			session.setAttribute("success", "Account Created Success");
			return "redirect:/login";
		} else {
			session.setAttribute("failure", "Invalid OTP! Try Again");
			return "redirect:/customer/enter-otp";
		}
	}
}

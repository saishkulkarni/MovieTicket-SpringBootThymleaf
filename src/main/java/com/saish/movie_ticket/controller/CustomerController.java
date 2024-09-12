package com.saish.movie_ticket.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.zxing.qrcode.encoder.QRCode;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.saish.movie_ticket.dto.Booking;
import com.saish.movie_ticket.dto.Customer;
import com.saish.movie_ticket.dto.Seat;
import com.saish.movie_ticket.dto.Show;
import com.saish.movie_ticket.helper.AES;
import com.saish.movie_ticket.helper.EmailSendingHelper;
import com.saish.movie_ticket.repository.BookingRepository;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.SeatRepository;
import com.saish.movie_ticket.repository.ShowRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	Customer customer;

	@Value("${razorpay.key}")
	private String key;

	@Value("${razorpay.secret}")
	private String secret;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	BookingRepository bookingRepository;

	@Autowired
	TheatreRepository theatreRepository;

	@Autowired
	EmailSendingHelper emailSendingHelper;

	@Autowired
	ShowRepository showRepository;

	@Autowired
	SeatRepository seatRepository;

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

	@GetMapping("/book-show/{showId}")
	public String bookShow(@PathVariable int showId, ModelMap map) {
		Show show = showRepository.findById(showId).orElseThrow();
		List<Seat> seats = show.getScreen().getSeats();
		map.put("show", show);
		map.put("seats", seats);
		return "seat-selection";
	}

	@PostMapping("/book-seats")
	public String bookSeats(@RequestParam int showId, @RequestParam List<String> selectedSeats, HttpSession session,
			ModelMap map)
			throws RazorpayException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer != null) {
			Show show = showRepository.findById(showId).orElseThrow();
			List<Seat> seats = seatRepository.findBySeatNumberIn(selectedSeats);

			for (Seat seat : seats) {
				if (seat.isOccupied()) {
					session.setAttribute("failure", "Selected seat(s) are already booked");
					return "redirect:/customer/book-show/" + showId;
				}
			}

			for (Seat seat : seats) {
				seat.setOccupied(true);
			}
			seatRepository.saveAll(seats);

			double price = selectedSeats.size() * show.getTicketPrice();

			Booking booking = new Booking();
			booking.setPrice(price);
			booking.setCustomer(customer);
			booking.setShow(show);
			booking.setSeatNumbers(seats);
			booking.setBookingTime(LocalDateTime.now());
			bookingRepository.save(booking);
			System.out.println("===============> "+booking.getId());
			List<Booking> list = customer.getBookingList();
			list.add(booking);
			customer.setBookingList(list);

			session.setAttribute("success", "Confirm Seat Details and do Payment");

			RazorpayClient razorpay = new RazorpayClient(key, secret);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", price * 100);
			orderRequest.put("currency", "INR");

			Order order = razorpay.orders.create(orderRequest);

			map.put("key", key);
			map.put("selectedSeats", selectedSeats);
			map.put("totalAmount", price);
			map.put("show", show);
			map.put("customer", customer);
			map.put("orderId", order.get("id"));
			map.put("bookingId", booking.getId());
			return "booking-confirmation-page.html";
		} else {
			session.setAttribute("failure", "First Login To Book Tickets");
			return "redirect:/login";
		}
	}

	@PostMapping("/confirm-booking/{bookingId}")
	public String confirmBooking(@PathVariable int bookingId, @RequestParam String razorpay_payment_id,
			@RequestParam String razorpay_order_id, @RequestParam String razorpay_signature, HttpSession session,
			ModelMap model) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer != null) {
			Booking booking = bookingRepository.findById(bookingId).orElseThrow();
			booking.setBooked(true);
			booking.setPaymentId(razorpay_payment_id);
			bookingRepository.save(booking);

			emailSendingHelper.sendBookingConfirmation(customer, booking);

			String qrCodeData = "Booking ID: " + bookingId + ", Customer: " + customer.getName();
			String qrCodeImage = generateQRCode(qrCodeData);
			model.put("qrCodeImage", qrCodeImage);
			model.put("booking", booking);
			session.setAttribute("success", "Booking confirmed successfully!");
			return "booking-details.html";
		} else {
			session.setAttribute("failure", "Invalid session. Please login again");
			return "redirect:/login";
		}
	}

	private String generateQRCode(String qrCodeData) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);

			ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
			byte[] pngData = pngOutputStream.toByteArray();

			return Base64.getEncoder().encodeToString(pngData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

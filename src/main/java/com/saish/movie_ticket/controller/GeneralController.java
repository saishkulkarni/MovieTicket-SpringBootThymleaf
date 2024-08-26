package com.saish.movie_ticket.controller;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.saish.movie_ticket.dto.Customer;
import com.saish.movie_ticket.dto.Movie;
import com.saish.movie_ticket.dto.Show;
import com.saish.movie_ticket.dto.Theatre;
import com.saish.movie_ticket.helper.AES;
import com.saish.movie_ticket.helper.CloudinaryHelper;
import com.saish.movie_ticket.helper.EmailSendingHelper;
import com.saish.movie_ticket.repository.CustomerRepository;
import com.saish.movie_ticket.repository.MovieRepository;
import com.saish.movie_ticket.repository.ShowRepository;
import com.saish.movie_ticket.repository.TheatreRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class GeneralController {

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	TheatreRepository theatreRepository;

	@Autowired
	CloudinaryHelper cloudinaryHelper;

	@Autowired
	ShowRepository showRepository;

	@Autowired
	MovieRepository movieRepository;

	@Autowired
	EmailSendingHelper emailSendingHelper;

	@Value("${admin.email}")
	private String adminEmail;
	@Value("${admin.password}")
	private String adminPassword;

	@GetMapping("/")
	public String loadMain(ModelMap map) {
		map.put("movies", movieRepository.findAll());
		return "home.html";
	}

	@GetMapping("/login")
	public String loadLogin() {
		return "login.html";
	}

	@PostMapping("/login")
	public String login(@RequestParam String emph, @RequestParam String password, HttpSession session) {
		try {
			long mobile = Long.parseLong(emph);
			Customer customer = customerRepository.findByMobile(mobile);
			Theatre theatre = theatreRepository.findByMobile(mobile);
			if (customer == null && theatre == null) {
				session.setAttribute("failure", "Invalid Mobile");
				return "redirect:/login";
			} else {
				if (customer != null) {
					if (AES.decrypt(customer.getPassword(), "123").equals(password)) {
						if (customer.isVerified()) {
							session.setAttribute("success", "Login Success As Customer");
							session.setAttribute("customer", customer);
							return "redirect:/";
						} else {
							customer.setOtp(new Random().nextInt(100000, 1000000));
							emailSendingHelper.sendMailToCustomer(customer);
							customerRepository.save(customer);
							session.setAttribute("success", "Otp Sent Success!!!");
							session.setAttribute("id", customer.getId());
							return "redirect:/customer/enter-otp";
						}

					} else {
						session.setAttribute("failure", "Invalid Password");
						return "redirect:/login";
					}
				} else {
					if (AES.decrypt(theatre.getPassword(), "123").equals(password)) {
						if (theatre.isVerified()) {

							if (theatre.isApproved()) {
								session.setAttribute("success", "Login Success As Theatre");
								session.setAttribute("theatre", theatre);
								return "redirect:/";
							} else {
								session.setAttribute("failure",
										"Approval is Under Process Wait for Sometime or Contact Admin");
								return "redirect:/login";
							}

						} else {
							theatre.setOtp(new Random().nextInt(100000, 1000000));
							emailSendingHelper.sendMailToTheatre(theatre);
							theatreRepository.save(theatre);
							session.setAttribute("success", "Otp Sent Success!!!");
							session.setAttribute("id", theatre.getId());
							return "redirect:/theatre/enter-otp";
						}

					} else {
						session.setAttribute("failure", "Invalid Password");
						return "redirect:/login";
					}
				}
			}

		} catch (NumberFormatException e) {
			String email = emph;
			if (email.equals(adminEmail) && password.equals(adminPassword)) {
				session.setAttribute("success", "Login Success As Admin");
				session.setAttribute("admin", "admin");
				return "redirect:/";
			} else {
				Customer customer = customerRepository.findByEmail(email);
				Theatre theatre = theatreRepository.findByEmail(email);
				if (customer == null && theatre == null) {
					session.setAttribute("failure", "Invalid Email");
					return "redirect:/login";
				} else {
					if (customer != null) {
						if (AES.decrypt(customer.getPassword(), "123").equals(password)) {
							if (customer.isVerified()) {
								session.setAttribute("success", "Login Success As Customer");
								session.setAttribute("customer", customer);
								return "redirect:/";
							} else {
								customer.setOtp(new Random().nextInt(100000, 1000000));
								emailSendingHelper.sendMailToCustomer(customer);
								customerRepository.save(customer);
								session.setAttribute("success", "Otp Sent Success!!!");
								session.setAttribute("id", customer.getId());
								return "redirect:/customer/enter-otp";
							}

						} else {
							session.setAttribute("failure", "Invalid Password");
							return "redirect:/login";
						}
					} else {
						if (AES.decrypt(theatre.getPassword(), "123").equals(password)) {
							if (theatre.isVerified()) {

								if (theatre.isApproved()) {
									session.setAttribute("success", "Login Success As Theatre");
									session.setAttribute("theatre", theatre);
									return "redirect:/";
								} else {
									session.setAttribute("failure",
											"Approval is Under Process Wait for Sometime or Contact Admin");
									return "redirect:/login";
								}

							} else {
								theatre.setOtp(new Random().nextInt(100000, 1000000));
								emailSendingHelper.sendMailToTheatre(theatre);
								theatreRepository.save(theatre);
								session.setAttribute("success", "Otp Sent Success!!!");
								session.setAttribute("id", theatre.getId());
								return "redirect:/theatre/enter-otp";
							}

						} else {
							session.setAttribute("failure", "Invalid Password");
							return "redirect:/login";
						}
					}
				}
			}

		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("customer");
		session.removeAttribute("admin");
		session.removeAttribute("theatre");
		session.setAttribute("success", "Logout Success");
		return "redirect:/";
	}

	@GetMapping("/admin/approve-theatre")
	public String approveTheatre(HttpSession session, ModelMap map) {
		if (session.getAttribute("admin") != null) {
			List<Theatre> list = theatreRepository.findByApprovedFalseAndVerifiedTrue();
			if (list.isEmpty()) {
				session.setAttribute("failure", "No Theatres Pending With Approve Request");
				return "redirect:/";
			} else {
				map.put("list", list);
				return "theatre-approve.html";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/admin/approve-theatre/{id}")
	public String approveTheatre(HttpSession session, ModelMap map, @PathVariable int id) {
		if (session.getAttribute("admin") != null) {
			Theatre theatre = theatreRepository.findById(id).orElseThrow();
			theatre.setApproved(true);
			theatreRepository.save(theatre);
			session.setAttribute("success", "Account Approved Success");
			return "redirect:/";

		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/admin/add-movie")
	public String addMovie(HttpSession session, ModelMap map) {
		if (session.getAttribute("admin") != null) {
			return "add-movie.html";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@PostMapping("/admin/add-movie")
	public String addMovie(HttpSession session, ModelMap map, Movie movie, @RequestParam MultipartFile image)
			throws IOException {
		if (session.getAttribute("admin") != null) {
			movie.setMovie_poster(cloudinaryHelper.saveMoviePosterToCloud(image));
			movieRepository.save(movie);
			session.setAttribute("success", "Movie Added Success");
			return "redirect:/";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/movies")
	public String loadAllMovies(ModelMap map, HttpSession session) {
		List<Movie> movies = movieRepository.findAll();
		if (movies.isEmpty()) {
			session.setAttribute("failure", "No Movies Are Running");
			return "redirect:/";
		} else {
			map.put("movies", movies);
			return "view-movies.html";
		}
	}

	@GetMapping("/shows/{id}")
	public String loadAllShows(ModelMap map, HttpSession session, @PathVariable int id) {
		Movie movie = movieRepository.findById(id).orElseThrow();
		List<Show> shows = showRepository.findByMovieAndAvailableTrue(movie);

		if (shows.isEmpty()) {
			session.setAttribute("failure", "There are No Shows Running");
			return "redirect:/";
		} else {
			map.put("shows", shows);
			return "view-shows.html";
		}
	}

}

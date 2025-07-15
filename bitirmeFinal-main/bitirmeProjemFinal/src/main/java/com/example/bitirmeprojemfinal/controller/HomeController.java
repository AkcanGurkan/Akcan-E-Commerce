package com.example.bitirmeprojemfinal.controller;

import com.example.bitirmeprojemfinal.entity.Category;
import com.example.bitirmeprojemfinal.entity.Product;
import com.example.bitirmeprojemfinal.entity.UserDtls;
import com.example.bitirmeprojemfinal.service.CartService;
import com.example.bitirmeprojemfinal.service.CategoryService;
import com.example.bitirmeprojemfinal.service.ProductService;
import com.example.bitirmeprojemfinal.service.UserService;
import com.example.bitirmeprojemfinal.util.CommonUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index(Model m) {

		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
		m.addAttribute("category", allActiveCategory);
		m.addAttribute("products", allActiveProducts);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/akcan-ai")
	public String akcanAi() {
		return "akcan-ai";
	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);
		Page<Product> page = null;
		if (StringUtils.isEmpty(ch)) {
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
		}

		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {

		Boolean existsEmail = userService.existsEmail(user.getEmail());

		if (existsEmail) {
			session.setAttribute("errorMsg", "Email Zaten Mevcut");
		} else {
			String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveUser(user);

			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());

//					System.out.println(path);
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}
				session.setAttribute("succMsg", "Başarıyla Kaydoldu");
			} else {
				session.setAttribute("errorMsg", "Sunucuda Bir Hata Var");
			}
		}

		return "redirect:/register";
	}

//	Forgot Password Code 

	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {

		UserDtls userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Geçersiz E-posta");
		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// Generate URL :
			// http://localhost:8080/reset-password?token=sfgdbgfswegfbdgfewgvsrg

			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if (sendMail) {
				session.setAttribute("succMsg", "Lütfen e-postanızı kontrol edin.. Şifre sıfırlama bağlantısı gönderildi");
			} else {
				session.setAttribute("errorMsg", "E-posta gönderilemedi");
			}
		}

		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {

		UserDtls userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("msg", "Bağlantınız geçersiz veya süresi dolmuş");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Model m) {

		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("errorMsg", "Bağlantınız geçersiz veya süresi dolmuş");
			return "message";
		} else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			// session.setAttribute("succMsg", "Password change successfully");
			m.addAttribute("msg", "Şifre Başarıyla Değiştirildi");

			return "message";
		}

	}

	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Model m) {
		List<Product> searchProducts = productService.searchProduct(ch);
		m.addAttribute("products", searchProducts);
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
		return "product";
	}

	@GetMapping("/filter-products")
	public String filterProducts(
			@RequestParam(required = false) Integer minPrice,
			@RequestParam(required = false) Integer maxPrice,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) Boolean inStock,
			@RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			Model m) {
		
		List<Product> allProducts = productService.getAllActiveProducts(category);
		List<Product> filteredProducts = new java.util.ArrayList<>(allProducts);
		
		// Apply price filter if provided
		if (minPrice != null && maxPrice != null) {
			filteredProducts = filteredProducts.stream()
					.filter(p -> p.getDiscountPrice() >= minPrice && p.getDiscountPrice() <= maxPrice)
					.toList();
		} else if (minPrice != null) {
			filteredProducts = filteredProducts.stream()
					.filter(p -> p.getDiscountPrice() >= minPrice)
					.toList();
		} else if (maxPrice != null) {
			filteredProducts = filteredProducts.stream()
					.filter(p -> p.getDiscountPrice() <= maxPrice)
					.toList();
		}
		
		// Apply stock filter if provided
		if (inStock != null && inStock) {
			filteredProducts = filteredProducts.stream()
					.filter(p -> p.getStock() > 0)
					.toList();
		}
		
		// Apply sorting if provided
		if (sort != null && !sort.isEmpty()) {
			switch (sort) {
				case "price_asc":
					filteredProducts = filteredProducts.stream()
							.sorted((p1, p2) -> Double.compare(p1.getDiscountPrice(), p2.getDiscountPrice()))
							.toList();
					break;
				case "price_desc":
					filteredProducts = filteredProducts.stream()
							.sorted((p1, p2) -> Double.compare(p2.getDiscountPrice(), p1.getDiscountPrice()))
							.toList();
					break;
				case "name_asc":
					filteredProducts = filteredProducts.stream()
							.sorted((p1, p2) -> p1.getTitle().compareTo(p2.getTitle()))
							.toList();
					break;
				case "name_desc":
					filteredProducts = filteredProducts.stream()
							.sorted((p1, p2) -> p2.getTitle().compareTo(p1.getTitle()))
							.toList();
					break;
				case "newest":
					filteredProducts = filteredProducts.stream()
							.sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
							.toList();
					break;
			}
		}
		
		// Calculate pagination values
		int totalElements = filteredProducts.size();
		int totalPages = (int) Math.ceil((double) totalElements / pageSize);
		
		// Ensure pageNo is within bounds
		if (pageNo < 0) {
			pageNo = 0;
		} else if (pageNo >= totalPages && totalPages > 0) {
			pageNo = totalPages - 1;
		}
		
		// Apply pagination to filtered products
		List<Product> paginatedProducts;
		if (!filteredProducts.isEmpty()) {
			int start = pageNo * pageSize;
			int end = Math.min(start + pageSize, filteredProducts.size());
			
			if (start < filteredProducts.size()) {
				paginatedProducts = filteredProducts.subList(start, end);
			} else {
				paginatedProducts = new java.util.ArrayList<>();
			}
		} else {
			paginatedProducts = new java.util.ArrayList<>();
		}
		
		// Set all required attributes for the template
		m.addAttribute("products", paginatedProducts);
		m.addAttribute("productsSize", paginatedProducts.size());
		m.addAttribute("paramValue", category);
		m.addAttribute("pageNo", pageNo);
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", totalElements);
		m.addAttribute("totalPages", totalPages);
		m.addAttribute("isFirst", pageNo == 0);
		m.addAttribute("isLast", pageNo == totalPages - 1 || totalPages == 0);
		
		// Add filter parameters to model for form persistence
		m.addAttribute("minPrice", minPrice);
		m.addAttribute("maxPrice", maxPrice);
		m.addAttribute("sort", sort);
		m.addAttribute("inStock", inStock);
		
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
		
		return "product";
	}

}

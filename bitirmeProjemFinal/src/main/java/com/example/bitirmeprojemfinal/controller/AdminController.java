package com.example.bitirmeprojemfinal.controller;


import com.example.bitirmeprojemfinal.entity.Category;
import com.example.bitirmeprojemfinal.entity.Product;
import com.example.bitirmeprojemfinal.entity.ProductOrder;
import com.example.bitirmeprojemfinal.entity.UserDtls;
import com.example.bitirmeprojemfinal.service.*;
import com.example.bitirmeprojemfinal.util.CommonUtil;
import com.example.bitirmeprojemfinal.util.OrderStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${app.upload.dir}")
	private String uploadDir;

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
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {
			session.setAttribute("errorMsg", "Kategori Adı Zaten Mevcut");
		} else {

			Category saveCategory = categoryService.saveCategory(category);

			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Kaydedilemedi!!");
			} else {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("succMsg", "Başarıyla Kaydedildi");
			}
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "Kategori Silme Başarılı");
		} else {
			session.setAttribute("errorMsg", "Serviste Sorun Var");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {

			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "Kategori Güncelleme Başarılı");
		} else {
			session.setAttribute("errorMsg", "Sunucu Hatası");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
							  HttpSession session) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct) && !image.isEmpty()) {
			try {
				// Ürün resimleri klasörünü oluştur
				File productImgDir = new File(uploadDir, "product_img");
				if (!productImgDir.exists()) {
					productImgDir.mkdirs();
				}
				
				// Resmi kaydet
				File destFile = new File(productImgDir, imageName);
				Files.copy(image.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				session.setAttribute("succMsg", "Ürün Başarıyla Kaydedildi");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("errorMsg", "Resim yüklenirken hata oluştu: " + e.getMessage());
				return "redirect:/admin/loadAddProduct";
			}
		} else {
			session.setAttribute("errorMsg", "Sunucu Hatası");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Ürün Başarıyla Silindi");
		} else {
			session.setAttribute("errorMsg", "Sunucu Hatası");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "invalid Discount");
		} else {
			try {
				if (!image.isEmpty()) {
					// Ürün resimleri klasörünü oluştur
					File productImgDir = new File(uploadDir, "product_img");
					if (!productImgDir.exists()) {
						productImgDir.mkdirs();
					}
					
					// Resmi kaydet
					File destFile = new File(productImgDir, image.getOriginalFilename());
					image.transferTo(destFile);
				}
				
				Product updateProduct = productService.updateProduct(product, image);
				if (!ObjectUtils.isEmpty(updateProduct)) {
					session.setAttribute("succMsg", "Ürün Güncellendi");
				} else {
					session.setAttribute("errorMsg", "Sunucu Hatası");
				}
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("errorMsg", "Resim güncellenirken hata oluştu: " + e.getMessage());
			}
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		List<UserDtls> users = null;
		if (type == 1) {
			users = userService.getUsers("ROLE_USER");
		} else {
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Hesap Durumu Güncellendi");
		} else {
			session.setAttribute("errorMsg", "Sunucu Hatası");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);

		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Durum Güncellendi");
		} else {
			session.setAttribute("errorMsg", "Durum Güncellenmedi !");
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);

			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());

		}
		return "/admin/orders";

	}

	@GetMapping("/add-admin")
	public String loadAdminAdd() {
		return "/admin/add_admin";
	}

	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveAdmin(user);

		if (!ObjectUtils.isEmpty(saveUser)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

//				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Başarıyla Kaydoldu");
		} else {
			session.setAttribute("errorMsg", "Sunucu Hatası");
		}

		return "redirect:/admin/add-admin";
	}

	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profil Güncellenmedi");
		} else {
			session.setAttribute("succMsg", "Profil Güncellendi");
		}
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Şifre Güncellenmedi Sunucu Hatası");
			} else {
				session.setAttribute("succMsg", "Şifre Başarıyla Güncellendi");
			}
		} else {
			session.setAttribute("errorMsg", "Mevcut Şifre Yanlış");
		}

		return "redirect:/admin/profile";
	}

	@GetMapping("/deleteProductImage/{id}")
	public String deleteProductImage(@PathVariable int id, HttpSession session) {
		Product product = productService.getProductById(id);
		if (product != null) {
			try {
				// Ürün resimleri klasörünü kontrol et
				File productImgDir = new File(uploadDir, "product_img");
				if (!productImgDir.exists()) {
					productImgDir.mkdirs();
				}
				
				// Resim dosyasını bul
				File imageFile = new File(productImgDir, product.getImage());
				
				// Eğer dosya varsa ve default.jpg değilse sil
				if (imageFile.exists() && !product.getImage().equals("default.jpg")) {
					Files.delete(imageFile.toPath());
				}
				
				// Ürünün image alanını güncelle
				product.setImage("default.jpg");
				productService.saveProduct(product);
				
				session.setAttribute("succMsg", "Ürün fotoğrafı başarıyla silindi");
			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("errorMsg", "Fotoğraf silinirken bir hata oluştu: " + e.getMessage());
			}
		} else {
			session.setAttribute("errorMsg", "Ürün bulunamadı");
		}
		return "redirect:/admin/editProduct/" + id;
	}

	@GetMapping("/orders/{orderId}/details")
	@ResponseBody
	public Map<String, Object> getOrderDetails(@PathVariable String orderId) {
		ProductOrder order = orderService.getOrdersByOrderId(orderId);
		if (order != null) {
			Map<String, Object> response = new HashMap<>();
			response.put("id", order.getId());
			response.put("orderId", order.getOrderId());
			response.put("customerName", order.getOrderAddress().getFirstName() + " " + order.getOrderAddress().getLastName());
			response.put("email", order.getOrderAddress().getEmail());
			response.put("phone", order.getOrderAddress().getMobileNo());
			response.put("shippingAddress", order.getOrderAddress().getAddress());
			response.put("city", order.getOrderAddress().getCity());
			response.put("state", order.getOrderAddress().getState());
			response.put("pincode", order.getOrderAddress().getPincode());
			response.put("status", order.getStatus());
			response.put("orderDate", order.getOrderDate());
			
			// Sipariş ürünlerini listeye ekle
			List<Map<String, Object>> items = new ArrayList<>();
			Map<String, Object> item = new HashMap<>();
			item.put("productName", order.getProduct().getTitle());
			item.put("quantity", order.getQuantity());
			item.put("unitPrice", order.getPrice());
			item.put("totalPrice", order.getPrice() * order.getQuantity());
			items.add(item);
			response.put("items", items);
			
			// Toplam tutarları hesapla
			double subtotal = order.getPrice() * order.getQuantity();
			double tax = subtotal * 0.18; // %18 KDV
			double shippingCost = 250.00; // Sabit kargo ücreti
			double total = subtotal + tax + shippingCost;
			
			response.put("subtotal", subtotal);
			response.put("tax", tax);
			response.put("shippingCost", shippingCost);
			response.put("total", total);
			
			return response;
		}
		return null;
	}

}

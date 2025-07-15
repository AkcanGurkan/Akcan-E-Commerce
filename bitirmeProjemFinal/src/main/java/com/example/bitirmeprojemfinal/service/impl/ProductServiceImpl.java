package com.example.bitirmeprojemfinal.service.impl;


import com.example.bitirmeprojemfinal.entity.Product;
import com.example.bitirmeprojemfinal.entity.Cart;
import com.example.bitirmeprojemfinal.repository.ProductRepository;
import com.example.bitirmeprojemfinal.repository.CartRepository;
import com.example.bitirmeprojemfinal.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CartRepository cartRepository;

	@Value("${app.upload.dir}")
	private String uploadDir;

	@Override
	public Product saveProduct(Product product) {
		try {
			return productRepository.save(product);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		Product product = productRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(product)) {
			try {
				// First delete all cart entries for this product
				List<Cart> cartItems = cartRepository.findByProductId(id);
				if (!cartItems.isEmpty()) {
					cartRepository.deleteAll(cartItems);
				}
				
				// Then delete the product
				productRepository.delete(product);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	@Override
	public Product getProductById(Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile image) {
		try {
			Product dbProduct = getProductById(product.getId());
			
			// Handle image name
			String imageName = image.isEmpty() ? dbProduct.getImage() : image.getOriginalFilename();
			
			// Update product details
			dbProduct.setTitle(product.getTitle());
			dbProduct.setDescription(product.getDescription());
			dbProduct.setCategory(product.getCategory());
			dbProduct.setPrice(product.getPrice());
			dbProduct.setStock(product.getStock());
			dbProduct.setImage(imageName);
			dbProduct.setIsActive(product.getIsActive());
			dbProduct.setDiscount(product.getDiscount());

			// Calculate discount price
			Double discount = product.getPrice() * (product.getDiscount() / 100.0);
			Double discountPrice = product.getPrice() - discount;
			dbProduct.setDiscountPrice(discountPrice);

			// Save product first
			Product updateProduct = productRepository.save(dbProduct);

			// Yeni bir resim varsa dosya yüklemeyi yönetin
			if (!ObjectUtils.isEmpty(updateProduct) && !image.isEmpty()) {
				try {
					// Create product images directory if it doesn't exist
					File productImgDir = new File(uploadDir, "product_img");
					if (!productImgDir.exists()) {
						productImgDir.mkdirs();
					}

					// Create destination path
					Path destinationPath = Paths.get(productImgDir.getAbsolutePath(), imageName);
					
					// Copy file using NIO
					Files.copy(image.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
					// Log error but don't fail the update
					System.err.println("Error saving image: " + e.getMessage());
				}
			}
			
			return updateProduct;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products = null;
		if (ObjectUtils.isEmpty(category)) {
			products = productRepository.findByIsActiveTrue();
		} else {
			products = productRepository.findByCategory(category);
		}

		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {

		Page<Product> pageProduct = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
				ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = productRepository.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = productRepository.findByCategory(pageable, category);
//		}
		return pageProduct;
	}

}

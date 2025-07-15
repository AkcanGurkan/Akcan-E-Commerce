package com.example.bitirmeprojemfinal.controller;

import com.example.bitirmeprojemfinal.entity.Product;
import com.example.bitirmeprojemfinal.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @GetMapping("/most-expensive")
    public Product getMostExpensiveProduct() {
        List<Product> products = productService.getAllActiveProducts("");
        return products.stream()
                .filter(p -> p.getIsActive() && p.getStock() > 0)
                .max(Comparator.comparing(Product::getDiscountPrice))
                .orElse(null);
    }
} 
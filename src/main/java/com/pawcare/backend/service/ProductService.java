package com.pawcare.backend.service;

import com.pawcare.backend.dto.ProductRequest;
import com.pawcare.backend.entity.Product;
import com.pawcare.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.pawcare.backend.specification.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;

import com.pawcare.backend.exception.ProductNotFoundException;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(ProductRequest request) {

        Product product = new Product();

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImage(request.getImage());
        product.setBrand(request.getBrand());
        product.setExpiryDate(request.getExpiryDate());

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found")
                );

        existingProduct.setProductName(request.getProductName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImage(request.getImage());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setExpiryDate(request.getExpiryDate());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw  new ProductNotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }


    public List<Product> searchProducts(
            String keyword,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {

        Specification<Product> specification =
                Specification.where(
                                ProductSpecification.hasKeyword(keyword)
                        )
                        .and(
                                ProductSpecification.hasCategory(category)
                        )
                        .and(
                                ProductSpecification.hasBrand(brand)
                        )
                        .and(
                                ProductSpecification
                                        .priceGreaterThanOrEqual(minPrice)
                        )
                        .and(
                                ProductSpecification
                                        .priceLessThanOrEqual(maxPrice)
                        );

        return productRepository.findAll(specification);
    }


    public Product updateStock(
            Long id,
            Integer stockQuantity
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found")
                );

        product.setStockQuantity(stockQuantity);

        return productRepository.save(product);
    }
}
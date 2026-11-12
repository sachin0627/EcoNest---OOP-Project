package com.econest.controller;

import com.econest.dao.ProductDAO;
import com.econest.model.Product;

import java.util.List;

/**
 * Controller for Module 01: Product Catalogue Management.
 */
public class ProductController {
    private final ProductDAO productDAO;

    public ProductController() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public Product getProduct(String id) {
        return productDAO.getProductById(id);
    }

    public List<Product> searchProducts(String keyword, String category) {
        return productDAO.searchProducts(keyword, category);
    }

    public boolean saveProduct(Product product, boolean isNew) {
        if (product == null || !product.isValid()) {
            return false;
        }
        if (isNew) {
            return productDAO.addProduct(product);
        } else {
            return productDAO.updateProduct(product);
        }
    }

    public boolean deleteProduct(String productId) {
        return productDAO.deleteProduct(productId);
    }

    public String generateNextId() {
        return productDAO.getNextProductId();
    }
}

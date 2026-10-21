package com.marketplace.product.controller;

import com.marketplace.product.api.ProductApi;

import com.marketplace.product.model.ProductCreate;
import com.marketplace.product.model.ProductPageResponse;
import com.marketplace.product.model.ProductResponse;
import com.marketplace.product.model.ProductStatus;
import com.marketplace.product.model.ProductUpdate;
import com.marketplace.product.service.IProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final IProductService productService;

    @Override
    public ResponseEntity<ProductResponse> createProduct(ProductCreate productCreate) {
        ProductResponse response = productService.create(productCreate);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(UUID id) {
        productService.softDelete(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductResponse> getProductById(UUID id) {
        ProductResponse response = productService.getById(id);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductPageResponse> getProducts(
            Integer page,
            Integer size,
            ProductStatus status,
            String category) {
        ProductPageResponse response = productService.getProducts(page, size, status, category);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(UUID id, ProductUpdate productUpdate) {
        ProductResponse response = productService.update(id, productUpdate);

        return ResponseEntity.ok(response);
    }
}
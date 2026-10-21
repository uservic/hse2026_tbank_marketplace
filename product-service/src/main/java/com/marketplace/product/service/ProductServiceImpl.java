package com.marketplace.product.service;

import com.marketplace.product.entity.ProductEntity;
import com.marketplace.product.exception.BusinessException;
import com.marketplace.product.model.ProductCreate;
import com.marketplace.product.model.ProductPageResponse;
import com.marketplace.product.model.ProductResponse;
import com.marketplace.product.model.ProductStatus;
import com.marketplace.product.model.ProductUpdate;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

    private static final UUID FAKE_SELLER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Override
    @Transactional
    public ProductResponse create(ProductCreate dto) {
        ProductEntity entity = ProductEntity.builder()
                .sellerId(FAKE_SELLER_ID)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .status(dto.getStatus())
                .build();
        // for actual 'createdAt', 'updatedAt'
        productRepository.saveAndFlush(entity);

        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        ProductEntity entity = findOrThrow(id);

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductUpdate dto) {
        // entity is "managed"
        ProductEntity entity = findOrThrow(id);

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategory(dto.getCategory());
        entity.setStatus(dto.getStatus());

        entity = productRepository.saveAndFlush(entity);

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        // entity is "managed"
        ProductEntity entity = findOrThrow(id);

        entity.setStatus(ProductStatus.ARCHIVED);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse getProducts(Integer page, Integer size, ProductStatus status, String category) {
        // Создаем объект пагинации Spring Data (номер страницы, размер)
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductEntity> entityPage = productRepository.findByFilters(status, category, pageable);

        List<ProductResponse> dtoList = entityPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new ProductPageResponse()
                .totalElements((int) entityPage.getTotalElements())
                .page(page)
                .size(size)
                .content(dtoList);
    }

    private ProductEntity findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "Товар не найден по ID"));
    }

    private ProductResponse mapToResponse(ProductEntity entity) {
        return new ProductResponse()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
    }
}

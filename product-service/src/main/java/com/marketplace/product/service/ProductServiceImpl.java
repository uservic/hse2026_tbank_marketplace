package com.marketplace.product.service;

import com.marketplace.product.exception.BusinessException;
import com.marketplace.product.model.ProductCreate;
import com.marketplace.product.model.ProductPageResponse;
import com.marketplace.product.model.ProductResponse;
import com.marketplace.product.model.ProductStatus;
import com.marketplace.product.model.ProductUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class ProductServiceImpl implements IProductService {

    private final Map<UUID, ProductResponse> storage = new ConcurrentHashMap<>();

    @Override
    public ProductResponse create(ProductCreate dto) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ProductResponse product = new ProductResponse()
                .id(id)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .status(dto.getStatus())
                .createdAt(now)
                .updatedAt(now);

        storage.put(id, product);

        return product;
    }

    @Override
    public ProductResponse getById(UUID id) {
        ProductResponse product = storage.get(id);
        if (product == null) {
            throw new BusinessException("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "Товар не найден по ID");

        }

        return product;
    }

    @Override
    public ProductResponse update(UUID id, ProductUpdate dto) {
        ProductResponse existing = getById(id);

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        existing.setCategory(dto.getCategory());
        existing.setStatus(dto.getStatus());
        existing.setUpdatedAt(OffsetDateTime.now());

        storage.put(id, existing);

        return existing;
    }

    @Override
    public void delete(UUID id) {
        ProductResponse existing = getById(id);

        existing.setStatus(ProductStatus.ARCHIVED);
        existing.setUpdatedAt(OffsetDateTime.now());
        storage.put(id, existing);
    }

    @Override
    public ProductPageResponse getProducts(Integer page, Integer size, ProductStatus status, String category) {
        Stream<ProductResponse> stream = storage.values().stream();

        // 1. Фильтрация по статусу и категории
        if (status != null) {
            stream = stream.filter(p -> p.getStatus() == status);
        }
        if (category != null && !category.isBlank()) {
            stream = stream.filter(p -> category.equals(p.getCategory()));
        }

        List<ProductResponse> filteredList = stream.toList();
        int totalElements = filteredList.size();

        // 2. Ручная пагинация в оперативной памяти (пропускаем n элементов и берем размер страницы)
        int fromIndex = page * size;
        List<ProductResponse> pagedList = filteredList.stream()
                .skip(fromIndex)
                .limit(size)
                .toList();

        return new ProductPageResponse()
                .totalElements(totalElements)
                .page(page)
                .size(size)
                .content(pagedList);
    }
}

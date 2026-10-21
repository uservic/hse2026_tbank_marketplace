package com.marketplace.product.repository;

import com.marketplace.product.entity.ProductEntity;
import com.marketplace.product.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Выполняет динамическую фильтрацию товаров с поддержкой пагинации.
     * Если параметр равен null, условие фильтра автоматически игнорируется.
     */
    @Query("SELECT p FROM ProductEntity p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:category IS NULL OR p.category = :category)")
    Page<ProductEntity> findByFilters(
            @Param("status") ProductStatus status,
            @Param("category") String category,
            Pageable pageable
    );
}

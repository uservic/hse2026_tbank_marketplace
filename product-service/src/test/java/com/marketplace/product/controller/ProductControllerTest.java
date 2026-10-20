package com.marketplace.product.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String createdProductId;

    @Test
    @Order(1)
    void shouldCreateProductSuccessfully() throws Exception {
        String validProductJson = """
            {
              "name": "Тестовый телефон",
              "description": "Интеграционный тест",
              "price": 49999.99,
              "stock": 10,
              "category": "Смартфоны",
              "status": "ACTIVE"
            }
            """;

        String responseContent = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Тестовый телефон")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"id\":\"([^\"]+)\"").matcher(responseContent);
        if (matcher.find()) {
            createdProductId = matcher.group(1);
        }
    }

    @Test
    @Order(2)
    void shouldReturnValidationErrorForInvalidData() throws Exception {
        String invalidProductJson = """
            {
              "name": "",
              "price": -100.00,
              "stock": -5,
              "category": "Тест",
              "status": "ACTIVE"
            }
            """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details", hasKey("name")))
                .andExpect(jsonPath("$.details", hasKey("price")))
                .andExpect(jsonPath("$.details", hasKey("stock")));
    }

    @Test
    @Order(3)
    void shouldGetProductByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/products/" + createdProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdProductId)))
                .andExpect(jsonPath("$.name", is("Тестовый телефон")));
    }

    @Test
    @Order(4)
    void shouldReturn404ForNonExistingProduct() throws Exception {
        // Тест Пункта 5 ТЗ: Проверка генерации кастомной ошибки PRODUCT_NOT_FOUND
        String randomUuid = java.util.UUID.randomUUID().toString();
        mockMvc.perform(get("/products/" + randomUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code", is("PRODUCT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Товар не найден по ID")));
    }

    @Test
    @Order(5)
    void shouldUpdateProductSuccessfully() throws Exception {
        // Тест эндпоинта PUT /products/{id}
        String updateProductJson = """
            {
              "name": "Обновленный телефон",
              "description": "Новое описание",
              "price": 55000.00,
              "stock": 15,
              "category": "Смартфоны",
              "status": "INACTIVE"
            }
            """;

        mockMvc.perform(put("/products/" + createdProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateProductJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdProductId)))
                .andExpect(jsonPath("$.name", is("Обновленный телефон")))
                .andExpect(jsonPath("$.status", is("INACTIVE")));
    }

    @Test
    @Order(6)
    void shouldSoftDeleteProduct() throws Exception {
        mockMvc.perform(delete("/products/" + createdProductId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + createdProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARCHIVED")));
    }

    @Test
    @Order(7)
    void shouldGetProductsListWithFiltersAndPagination() throws Exception {
        // Тест сложного эндпоинта GET /products с пагинацией и фильтром по заархивированному статусу
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ARCHIVED")
                        .param("category", "Смартфоны"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(createdProductId)))
                .andExpect(jsonPath("$.content[0].status", is("ARCHIVED")));
    }
}

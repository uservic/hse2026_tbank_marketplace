package com.marketplace.product.service;

import com.marketplace.product.model.ProductCreate;
import com.marketplace.product.model.ProductPageResponse;
import com.marketplace.product.model.ProductResponse;
import com.marketplace.product.model.ProductStatus;
import com.marketplace.product.model.ProductUpdate;

import java.util.UUID;

/**
 * Сервис управления товарным каталогом маркетплейса.
 * Обеспечивает жизненный цикл сущности {@link ProductResponse} и контрактную валидацию.
 */
public interface IProductService {

    /**
     * Создает новый товар в каталоге маркетплейса.
     *
     * @param productCreate DTO-схема запроса, содержащая обязательные поля товара (name, price, stock, category, status)
     * @return {@link ProductResponse} — полная информация о созданном товаре, включая сгенерированный UUID и временные метки
     */
    ProductResponse create(ProductCreate productCreate);

    /**
     * Возвращает информацию о товаре по его уникальному идентификатору.
     * Если товар не найден в системе, выбрасывается бизнес-исключение с кодом PRODUCT_NOT_FOUND.
     *
     * @param id уникальный идентификатор товара в формате UUID
     * @return {@link ProductResponse} — DTO-модель запрашиваемого товара
     * @throws com.marketplace.product.exception.BusinessException если товар с указанным ID отсутствует в базе данных (HTTP 404)
     */
    ProductResponse getById(UUID id);

    /**
     * Обновляет параметры существующего товара в каталоге.
     * Изменению подлежат только мутирующие поля (название, описание, цена, остаток, категория, статус).
     * Временная метка обновления (updated_at) перезаписывается текущим временем.
     *
     * @param id            уникальный идентификатор обновляемого товара в формате UUID
     * @param productUpdate DTO-схема запроса с новыми значениями полей товара
     * @return {@link ProductResponse} — обновленная DTO-модель товара
     * @throws com.marketplace.product.exception.BusinessException если товар с указанным ID не найден в системе (HTTP 404)
     */
    ProductResponse update(UUID id, ProductUpdate productUpdate);

    /**
     * Выполняет мягкое удаление товара из каталога маркетплейса.
     * Физическая запись остается в системе, но статус товара принудительно переводится в {@link ProductStatus#ARCHIVED}.
     * Временная метка обновления (updated_at) перезаписывается текущим временем.
     *
     * @param id уникальный идентификатор архивируемого товара в формате UUID
     * @throws com.marketplace.product.exception.BusinessException если товар с указанным ID не найден в системе (HTTP 404)
     */
    void softDelete(UUID id);

    /**
     * Возвращает список товаров, соответствующих заданным критериям фильтрации, с поддержкой пагинации.
     * Пагинация и точная фильтрация выполняются согласно требованиям Пункта 1 технического задания.
     *
     * @param page     номер запрашиваемой страницы, начиная с 0 (параметр по умолчанию: 0)
     * @param size     максимальный размер страницы (параметр по умолчанию: 20)
     * @param status   опциональный фильтр по состоянию товара (ACTIVE, INACTIVE, ARCHIVED)
     * @param category опциональный фильтр по названию категории (точное совпадение)
     * @return {@link ProductPageResponse} — контрактный объект ответа, содержащий отфильтрованный список товаров,
     * общее количество элементов (totalElements), текущую страницу и размер страницы
     */
    ProductPageResponse getProducts(Integer page, Integer size, ProductStatus status, String category);
}
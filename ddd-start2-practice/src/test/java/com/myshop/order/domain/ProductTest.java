package com.myshop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class ProductTest {

    @Test
    void 상품_ID_null일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ProductId(null);
        });
    }

    @Test
    void 상품_ID_공백일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ProductId("");
        });
    }

    @Test
    void 상품_ID_프로퍼티_일치시_equal_true_테스트() {
        String productId = "product_001";

        ProductId productId1 = new ProductId(productId);
        ProductId productId2 = new ProductId(productId);

        assertThat(productId1).isEqualTo(productId2);
    }

    @Test
    void 상품_이름_null일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ProductName(null);
        });
    }

    @Test
    void 상품_이름_공백_일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ProductName("");
        });
    }

    @Test
    void 상품_이름_동일시_equals_true_테스트() {
        String productName = "productName";
        ProductName first = new ProductName(productName);
        ProductName second = new ProductName(productName);

        assertThat(first).isEqualTo(second);
    }
}

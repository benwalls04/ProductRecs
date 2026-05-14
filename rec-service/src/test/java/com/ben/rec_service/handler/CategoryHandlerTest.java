package com.ben.rec_service.handler;

import com.ben.rec_service.TestFixtures;
import com.ben.rec_service.model.Product;
import com.ben.rec_service.model.ProductRec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryHandlerTest {

    private final CategoryHandler handler = new CategoryHandler();

    @Test
    void returnsOnlyProductsMatchingClickedCategory() {
        Product match   = TestFixtures.product("Laptop", 999.0, "d", "electronics", 4.5);
        Product noMatch = TestFixtures.product("Shirt",   29.0, "d", "clothing",    4.9);
        Product clicked = TestFixtures.product("Phone",  499.0, "d", "electronics", 4.0);

        List<ProductRec> result = handler.recommend(5, clicked, List.of(match, noMatch));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getName()).isEqualTo("Laptop");
    }

    @Test
    void categoryMatchIsCaseInsensitive() {
        Product p       = TestFixtures.product("A", 10.0, "d", "Electronics", 4.0);
        Product clicked = TestFixtures.product("B", 20.0, "d", "electronics", 4.0);

        assertThat(handler.recommend(5, clicked, List.of(p))).hasSize(1);
    }

    @Test
    void sortsMatchesByRatingDescending() {
        Product low     = TestFixtures.product("Low",  10.0, "d", "electronics", 2.0);
        Product high    = TestFixtures.product("High", 20.0, "d", "electronics", 4.8);
        Product clicked = TestFixtures.product("X",    30.0, "d", "electronics", 4.0);

        List<ProductRec> result = handler.recommend(5, clicked, List.of(low, high));

        assertThat(result.get(0).getProduct().getName()).isEqualTo("High");
        assertThat(result.get(1).getProduct().getName()).isEqualTo("Low");
    }

    @Test
    void returnsEmpty_whenClickedProductIsNull() {
        Product p = TestFixtures.product("A", 10.0, "d", "electronics", 4.0);
        assertThat(handler.recommend(5, null, List.of(p))).isEmpty();
    }

    @Test
    void returnsEmpty_whenClickedProductHasNullCategory() {
        Product p       = TestFixtures.product("A", 10.0, "d", "electronics", 4.0);
        Product clicked = TestFixtures.product("B", 20.0);

        assertThat(handler.recommend(5, clicked, List.of(p))).isEmpty();
    }

    @Test
    void skipsProductsWithNullRating() {
        Product noRating = TestFixtures.product("NoRating", 10.0);
        noRating.setCategory("electronics");
        Product clicked = TestFixtures.product("X", 10.0, "d", "electronics", 4.0);

        assertThat(handler.recommend(5, clicked, List.of(noRating))).isEmpty();
    }

    @Test
    void limitsToN() {
        List<Product> products = List.of(
                TestFixtures.product("A", 10.0, "d", "electronics", 4.5),
                TestFixtures.product("B", 20.0, "d", "electronics", 4.0),
                TestFixtures.product("C", 30.0, "d", "electronics", 3.5)
        );
        Product clicked = TestFixtures.product("X", 10.0, "d", "electronics", 4.0);

        assertThat(handler.recommend(2, clicked, products)).hasSize(2);
    }
}

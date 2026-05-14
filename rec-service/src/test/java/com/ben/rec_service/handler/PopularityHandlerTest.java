package com.ben.rec_service.handler;

import com.ben.rec_service.TestFixtures;
import com.ben.rec_service.model.Product;
import com.ben.rec_service.model.ProductRec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PopularityHandlerTest {

    private final PopularityHandler handler = new PopularityHandler();

    @Test
    void returnsTopNByRatingDescending() {
        Product low  = TestFixtures.product("Low",  10.0, "d", "cat", 2.0);
        Product mid  = TestFixtures.product("Mid",  20.0, "d", "cat", 3.5);
        Product high = TestFixtures.product("High", 30.0, "d", "cat", 4.9);

        List<ProductRec> result = handler.recommend(2, null, List.of(low, high, mid));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProduct().getName()).isEqualTo("High");
        assertThat(result.get(1).getProduct().getName()).isEqualTo("Mid");
    }

    @Test
    void scoresMatchRating() {
        Product p = TestFixtures.product("A", 10.0, "d", "cat", 4.7);

        List<ProductRec> result = handler.recommend(1, null, List.of(p));

        assertThat(result.get(0).getScore()).isEqualTo(4.7);
    }

    @Test
    void skipsProductsWithNullRating() {
        Product noRating = TestFixtures.product("NoRating", 10.0);
        Product rated    = TestFixtures.product("Rated",    20.0, "d", "cat", 4.0);

        List<ProductRec> result = handler.recommend(5, null, List.of(noRating, rated));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getName()).isEqualTo("Rated");
    }

    @Test
    void limitsToN() {
        List<Product> products = List.of(
                TestFixtures.product("A", 10.0, "d", "cat", 4.5),
                TestFixtures.product("B", 20.0, "d", "cat", 4.0),
                TestFixtures.product("C", 30.0, "d", "cat", 3.5)
        );

        assertThat(handler.recommend(1, null, products)).hasSize(1);
    }

    @Test
    void returnsEmptyList_whenNoProducts() {
        assertThat(handler.recommend(5, null, List.of())).isEmpty();
    }
}

package com.ben.storeservice.model.data;

import com.ben.storeservice.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DummyRetriever Unit Tests")
class DummyRetrieverTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private DummyRetriever retriever;

    @Test
    void clientResponseNull() {
        mockRestClientChain(null);

        assertTrue(retriever.getAll().isEmpty());
    }

    @Test
    void getResponseNull() {
        DummyJsonResponse response = new DummyJsonResponse();
        response.setProducts(null);
        mockRestClientChain(response);

        assertTrue(retriever.getAll().isEmpty());
    }

    @Test
    void getAll_whenProductsExist_returnsMappedProducts() {
        DummyJsonProduct dp = new DummyJsonProduct();
        dp.setTitle("Widget");
        dp.setDescription("A widget");
        dp.setCategory("tools");
        dp.setRating(4.5);
        dp.setPrice(9.99);

        DummyJsonResponse response = new DummyJsonResponse();
        response.setProducts(List.of(dp));
        mockRestClientChain(response);

        List<Product> result = retriever.getAll();

        assertThat(result.size() == 1);
        Product p = result.get(0);
        assertThat(p.getName()).isEqualTo("Widget");
        assertThat(p.getDescription()).isEqualTo("A widget");
        assertThat(p.getCategory()).isEqualTo("tools");
        assertThat(p.getRating()).isEqualTo(4.5);
        assertThat(p.getPrice()).isEqualTo(9.99);
    }

    @Test
    void getAll_whenMultipleProducts_returnsAllMapped() {
        DummyJsonResponse response = new DummyJsonResponse();
        response.setProducts(List.of(buildDummyProduct("Widget"), buildDummyProduct("Gadget")));
        mockRestClientChain(response);

        assertEquals(2, retriever.getAll().size());
    }

    private void mockRestClientChain(DummyJsonResponse response) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products?limit=0")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(DummyJsonResponse.class)).thenReturn(response);
    }

    private DummyJsonProduct buildDummyProduct(String title) {
        DummyJsonProduct dp = new DummyJsonProduct();
        dp.setTitle(title);
        dp.setDescription("desc");
        dp.setCategory("cat");
        dp.setRating(3.0);
        dp.setPrice(1.99);
        return dp;
    }
}
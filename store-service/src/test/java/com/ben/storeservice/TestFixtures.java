package com.ben.storeservice;

import com.ben.storeservice.model.*;

import java.util.ArrayList;
import java.util.List;

public class TestFixtures {

    public static Store store(String name, StoreType type) {
        Store store = new Store();
        store.setStoreName(name);
        store.setStoreType(type);
        return store;
    }

    public static Store dummyJsonStore() {
        return store("DummyJSON", StoreType.DUMMY_JSON);
    }

    public static Catalog catalog(Store store) {
        Catalog catalog = new Catalog();
        catalog.setStore(store);
        return catalog;
    }

    public static Product product(String name, Double price, Catalog catalog) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCatalog(catalog);
        return product;
    }

    public static Product product(String name, Double price) {
        return product(name, price, null);
    }

    public static Product product(String name, Double price, String description, String category, Double rating) {
        Product p = product(name, price, null);
        p.setDescription(description);
        p.setCategory(category);
        p.setRating(rating);
        return p;
    }

    public static Product product(String name, Double price, String description, String category, Double rating, Catalog catalog) {
        Product p = product(name, price, description, category, rating);
        p.setCatalog(catalog);
        return p;
    }

    public static Catalog emptyCatalog(Store store) {
        Catalog catalog = new Catalog();
        catalog.setStore(store);
        catalog.setProductList(new ArrayList<>());
        return catalog;
    }

    public static Page page(String name, Store store) {
        Page page = new Page();
        page.setName(name);
        page.setStore(store);
        return page;
    }

    public static Page page(Store store) {
        return page("Home", store);
    }

    public static Store storeWithCatalog(String name, StoreType type) {
        Store store = store(name, type);
        Catalog catalog = catalog(store);
        store.setCatalog(catalog);
        return store;
    }

    public static RecModule recModule(String name, Integer n, RecType recType, Page page) {
        RecModule rec = new RecModule();
        rec.setName(name);
        rec.setN(n);
        rec.setRecType(recType);
        rec.setPage(page);
        return rec;
    }

    public static RecModule recModule(String name, Integer n, Page page) {
        return recModule(name, n, RecType.POPULARITY, page);
    }

    public static ProductRec productRec(Product product, double score) {
        ProductRec rec = new ProductRec();
        rec.setProduct(product);
        rec.setScore(score);
        return rec;
    }

    public static Catalog catalogWithProducts(Store store, int count) {
        Catalog catalog = catalog(store);
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            products.add(product("Product " + i, i * 10.0, catalog));
        }
        catalog.setProductList(products);
        return catalog;
    }
}

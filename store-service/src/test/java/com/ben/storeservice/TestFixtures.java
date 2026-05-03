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

    public static Store bestBuyStore() {
        return store("Best Buy", StoreType.BEST_BUY);
    }

    public static Store openFoodStore() {
        return store("Open Food", StoreType.OPEN_FOOD);
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

    public static RecModule recModule(String name, Integer n, RecType recType, Store store) {
        RecModule rec = new RecModule();
        rec.setName(name);
        rec.setN(n);
        rec.setRecType(recType);
        rec.setStore(store);
        return rec;
    }

    public static RecModule recModule(String name, Integer n, Store store) {
        return recModule(name, n, RecType.POPULARITY, store);
    }

    public static ProductRec productRec(Product product, double score) {
        ProductRec rec = new ProductRec();
        rec.setProduct(product);
        rec.setScore(score);
        return rec;
    }

    public static Store storeWithCatalog(String name, StoreType type) {
        Store store = store(name, type);
        Catalog catalog = catalog(store);
        store.setCatalog(catalog);
        return store;
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

package com.ben.storeservice.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DummyJsonResponse {
    private List<DummyJsonProduct> products;
}

package com.ben.storeservice.feign;

import com.ben.storeservice.model.ProductRec;
import com.ben.storeservice.model.RecRequest;
import com.ben.storeservice.model.RecType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "REC-SERVICE")
public interface RecServiceClient {
    @PostMapping("/rec/type/{recType}")
    List<ProductRec> getRecommendations(@PathVariable("recType") RecType recType, @RequestBody RecRequest request);
}

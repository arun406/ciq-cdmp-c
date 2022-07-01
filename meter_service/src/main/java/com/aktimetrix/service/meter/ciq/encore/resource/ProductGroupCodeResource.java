package com.aktimetrix.service.meter.ciq.encore.resource;


import com.aktimetrix.service.meter.ciq.encore.model.ProductGroupEncoreMaster;
import com.aktimetrix.service.meter.ciq.encore.service.ProductGroupEncoreMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RequestMapping("/reference-data/encore/product-groups")
@RestController
public class ProductGroupCodeResource {

    @Autowired
    ProductGroupEncoreMasterService service;

    /**
     * @return
     */
    @GetMapping
    public List<ProductGroupEncoreMaster> list() {
        return service.list();
    }

    /**
     * @param productGroupEncoreMaster
     * @return
     */
    @PostMapping
    public ResponseEntity add(ProductGroupEncoreMaster productGroupEncoreMaster) {
        service.add(productGroupEncoreMaster);
        return ResponseEntity.created(URI.create("/reference-data/encore/product-groups/" + productGroupEncoreMaster.getId())).build();
    }
}

package org.gamein.marketservergamein2022.web.controller.market;

import org.gamein.marketservergamein2022.core.dto.result.*;
import org.gamein.marketservergamein2022.core.iao.AuthInfo;
import org.gamein.marketservergamein2022.core.service.market.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("product")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "raw-materials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getRawMaterials(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(productService.getRawMaterials(authInfo.getTeam())),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "intermediate-products",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getIntermediateProducts() {
        return new ResponseEntity<>(
                ServiceResult.createResult(productService.getIntermediateProducts()),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "final-products",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResult> getFinalProducts(@ModelAttribute("authInfo") AuthInfo authInfo) {
        return new ResponseEntity<>(
                ServiceResult.createResult(productService.getFinalProducts(authInfo.getTeam().getId())),
                HttpStatus.OK
        );
    }
}

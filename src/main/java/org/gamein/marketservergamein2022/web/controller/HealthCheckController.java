package org.gamein.marketservergamein2022.web.controller;

import org.gamein.marketservergamein2022.core.dto.result.BaseResult;
import org.gamein.marketservergamein2022.core.dto.result.ServiceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HealthCheckController {

    @GetMapping()
    public ResponseEntity<BaseResult> checkHealth(){
        return new ResponseEntity<>(ServiceResult.createResult("Up"), HttpStatus.OK);
    }
}

package org.billing.hrs.controllers;

import org.billing.data.dto.PhoneBalanceDto;
import org.billing.hrs.services.HrsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(value = "/api/hrs")
public class HrsController {

    private final HrsService hrsService;

    public HrsController(HrsService hrsService) {
        this.hrsService = hrsService;
    }

    @PostMapping("tarifficate")
    public ResponseEntity<?> tarifficate(@RequestBody File file){
        PhoneBalanceDto list = hrsService.tarifficate(file);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}

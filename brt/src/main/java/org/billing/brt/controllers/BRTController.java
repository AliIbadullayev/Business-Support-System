package org.billing.brt.controllers;

import org.billing.brt.services.BillingService;
import org.billing.data.dto.PhoneBalanceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping(value = "/api/brt")
public class BRTController {
    final private BillingService billingService;

    public BRTController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("billing")
    public ResponseEntity<?> billing(@RequestBody File file){
        File cdrPlus = billingService.validateCdr(file);
        PhoneBalanceDto phoneBalances = billingService.furtherTariffication(cdrPlus);
        return new ResponseEntity<>(phoneBalances, HttpStatus.OK);
    }
}

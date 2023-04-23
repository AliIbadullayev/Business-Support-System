package org.billing.cdr.controllers;

import org.billing.cdr.services.GeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/cdr")
public class CDRController {
    final private GeneratorService generatorService;

    public CDRController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generateCdr() throws IOException {
        File cdr = generatorService.generateCDRFile();
        return new ResponseEntity<>(generatorService.furtherTariffication(cdr), HttpStatus.OK);
    }
}

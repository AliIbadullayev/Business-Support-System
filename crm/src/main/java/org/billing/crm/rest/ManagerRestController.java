package org.billing.crm.rest;

import lombok.extern.slf4j.Slf4j;
import org.billing.crm.services.AbonentService;
import org.billing.data.dto.AbonentAddDto;
import org.billing.data.dto.ChangeTariffDto;
import org.billing.data.models.SubscriberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Контроллер для запросов со стороны менеджера
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/manager")
public class ManagerRestController {

    private final AbonentService abonentService;

    public ManagerRestController(AbonentService abonentService) {
        this.abonentService = abonentService;
    }

    @PostMapping("abonent")
    public ResponseEntity<?> addAbonent(@RequestBody AbonentAddDto abonentAddDto) {
        SubscriberInfo subscriberInfo = abonentService.addAbonent(abonentAddDto);
        return new ResponseEntity<>(subscriberInfo, HttpStatus.OK);
    }

    @PatchMapping("tariff")
    public ResponseEntity<?> changeTariff(@RequestBody ChangeTariffDto changeTariffDto) {
        SubscriberInfo subscriberInfo = abonentService.changeTariff(changeTariffDto);
        return new ResponseEntity<>(subscriberInfo, HttpStatus.OK);
    }

}

package org.billing.crm.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.billing.crm.dto.BillingDto;
import org.billing.crm.services.AbonentService;
import org.billing.data.dto.AbonentAddDto;
import org.billing.data.dto.ChangeTariffDto;
import org.billing.data.dto.PhoneBalanceDto;
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
    public ResponseEntity<?> addAbonent(@RequestBody AbonentAddDto abonentAddDto) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = abonentService.addAbonent(abonentAddDto);
        return new ResponseEntity<>(subscriberInfo, HttpStatus.OK);
    }

    @PatchMapping("tariff")
    public ResponseEntity<?> changeTariff(@RequestBody ChangeTariffDto changeTariffDto) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = abonentService.changeTariff(changeTariffDto);
        return new ResponseEntity<>(subscriberInfo, HttpStatus.OK);
    }

    @PatchMapping("billing")
    public ResponseEntity<?> billing(@RequestBody BillingDto billingDto) throws JsonProcessingException {
        PhoneBalanceDto phoneBalances = abonentService.furtherBilling(billingDto);
        return new ResponseEntity<>(phoneBalances, HttpStatus.OK);
    }
}

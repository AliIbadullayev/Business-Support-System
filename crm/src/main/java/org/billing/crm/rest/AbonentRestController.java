package org.billing.crm.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.billing.crm.security.JwtTokenProvider;
import org.billing.crm.services.AbonentService;
import org.billing.data.dto.AbonentPayDto;
import org.billing.data.models.SubscriberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Контроллер для запросов со стороны абонента
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/abonent")
public class AbonentRestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AbonentService abonentService;

    public AbonentRestController(JwtTokenProvider jwtTokenProvider, AbonentService abonentService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.abonentService = abonentService;
    }

    @PatchMapping("pay")
    public ResponseEntity<?> payForNumber(@RequestBody AbonentPayDto abonentPayDto,
                                         HttpServletRequest request) {
        String username = jwtTokenProvider.getUsernameFromToken(jwtTokenProvider.resolveToken(request));
        SubscriberInfo subscriberInfo = abonentService.replenishAbonentBalance(abonentPayDto);

        return new ResponseEntity<>(subscriberInfo, HttpStatus.OK);
    }

}

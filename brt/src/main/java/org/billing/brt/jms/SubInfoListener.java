package org.billing.brt.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.billing.brt.services.BillingService;
import org.billing.data.models.SubscriberInfo;
import org.springframework.jms.annotation.JmsListener;

@Slf4j
public class SubInfoListener {
    private ObjectMapper mapper;
    private final BillingService billingService;

    public SubInfoListener(BillingService billingService) {
        this.billingService = billingService;
    }


    @PostConstruct
    public void onInit(){
        mapper = new ObjectMapper();
    }


    @JmsListener(destination = "${spring.artemis.embedded.queues}")
    public void addPurchase(String msg) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = mapper.readValue(msg, SubscriberInfo.class);
        log.info("Получены данные абонента {} с сервиса crm!", subscriberInfo.getNumber());
        billingService.updateSubscriberInfoFromCrm(subscriberInfo);
    }
}

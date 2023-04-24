package org.billing.crm.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.billing.crm.services.AbonentService;
import org.billing.data.models.SubscriberInfo;
import org.springframework.jms.annotation.JmsListener;

@Slf4j
public class SubInfoListener {
    private ObjectMapper mapper;
    private final AbonentService abonentService;

    public SubInfoListener(AbonentService abonentService) {
        this.abonentService = abonentService;
    }

    @PostConstruct
    public void onInit(){
        mapper = new ObjectMapper();
    }


    @JmsListener(destination = "${spring.artemis.embedded.queues}")
    public void addPurchase(String msg) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = mapper.readValue(msg, SubscriberInfo.class);
        log.info("Получены данные абонента {} с сервиса crm!", subscriberInfo.getNumber());
        abonentService.updateSubscriberInfoFromBrt(subscriberInfo);
    }
}

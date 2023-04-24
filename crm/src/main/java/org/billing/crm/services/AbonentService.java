package org.billing.crm.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.billing.crm.dto.BillingDto;
import org.billing.crm.exception.*;
import org.billing.data.dto.AbonentAddDto;
import org.billing.data.dto.AbonentPayDto;
import org.billing.data.dto.ChangeTariffDto;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.Abonent;
import org.billing.data.models.Report;
import org.billing.data.models.SubscriberInfo;
import org.billing.data.models.Tariff;
import org.billing.data.repositories.AbonentRepository;
import org.billing.data.repositories.ReportRepository;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.billing.data.repositories.TariffRepository;
import org.billing.data.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
public class AbonentService {
    private final AbonentRepository abonentRepository;
    private final SubscriberInfoRepository subscriberInfoRepository;
    private final TariffRepository tariffRepository;
    private final ReportRepository reportRepository;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper mapper;
    private RestTemplate restTemplate;
    @Value("${cdr.server.host}")
    private String CDR_HOST;
    @Value("${cdr.server.port}")
    private String CDR_PORT;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
    }

    public AbonentService(AbonentRepository abonentRepository, SubscriberInfoRepository subscriberInfoRepository, TariffRepository tariffRepository, ReportRepository reportRepository, JmsTemplate jmsTemplate, ObjectMapper mapper) {
        this.abonentRepository = abonentRepository;
        this.subscriberInfoRepository = subscriberInfoRepository;
        this.tariffRepository = tariffRepository;
        this.reportRepository = reportRepository;
        this.jmsTemplate = jmsTemplate;
        this.mapper = mapper;
    }

    /* Передача следующему сервису CDR информации про тарификацию*/
    public PhoneBalanceDto furtherBilling(BillingDto billingDto){
        if(billingDto.getAction().equals("run")) {
            log.info("Начато действие совершить тарификацию!");
        }
        String newUrl = CommonUtils.generateUrl(CDR_HOST, CDR_PORT, "api/cdr/generate");
        return restTemplate.getForObject(newUrl, PhoneBalanceDto.class);
    }

    public Report getLastReportByNumber(String phone){
        Report report = reportRepository.getFirstByNumberOrderByCreationTimeDesc(phone);
        if (report == null){
            throw new NotFoundReportException("Отчет по номеру "+ phone+ " не найден!");
        }
        return report;
    }

    public SubscriberInfo replenishAbonentBalance( AbonentPayDto abonentPayDto) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = findAbonent(abonentPayDto.getPhoneNumber());
        if(subscriberInfo == null)
            throw new NotFoundAbonentException("Не найден номер "+abonentPayDto.getPhoneNumber()+" по клиенту!");
        if (abonentPayDto.getMoney() <= 0F)
            throw new BadAbonentPayException("Сумма пополнения должна быть не меньше 0 рублей!");
        subscriberInfo.setMoney(subscriberInfo.getMoney()+abonentPayDto.getMoney());
        updateSubscriberInfo(subscriberInfo);

        jmsTemplate.convertAndSend("brt_sub_info_update", mapper.writeValueAsString(subscriberInfo));
        return subscriberInfo;
    }

    public SubscriberInfo addAbonent(AbonentAddDto abonentAddDto) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        if (abonentAddDto.getTariffId() == null || abonentAddDto.getBalance() == null || abonentAddDto.getPhoneNumber() == null)
            throw new BadAbonentAddException("Пожалуйста перепроверьте введенные данные" + abonentAddDto);
        if (findAbonent(abonentAddDto.getPhoneNumber()) != null)
            throw new BadAbonentAddException("Номер уже существует!");
        Optional<Tariff> tariff = tariffRepository.findById(abonentAddDto.getTariffId());
        if(tariff.isEmpty())
            throw new BadAbonentAddException("Тариф "+abonentAddDto.getTariffId()+" не найден!" );
        subscriberInfo.setTariff(tariff.get());
        if (abonentAddDto.getBalance() < 0)
            throw new BadAbonentAddException("Баланс нового пользователя не может быть отрицательным!");
        if (!abonentAddDto.getPhoneNumber().matches("^7[0-9]{10}\\b"))
            throw new BadAbonentAddException("Не корректный формат номера!");
        subscriberInfo.setMoney(abonentAddDto.getBalance());
        subscriberInfo.setNumber(abonentAddDto.getPhoneNumber());
        updateSubscriberInfo(subscriberInfo);

        jmsTemplate.convertAndSend("brt_sub_info_update", mapper.writeValueAsString(subscriberInfo));
        return subscriberInfo;
    }

    public SubscriberInfo changeTariff(ChangeTariffDto changeTariffDto) throws JsonProcessingException {
        SubscriberInfo subscriberInfo = findAbonent(changeTariffDto.getPhoneNumber());
        if (subscriberInfo == null)
            throw new BadChangeTariffException("Номера не действителен! Внесите номер в БД!");
        if (changeTariffDto.getTariffId() == null || changeTariffDto.getPhoneNumber() == null)
            throw new BadChangeTariffException("Пожалуйста перепроверьте введенные данные" + changeTariffDto);
        Optional<Tariff> tariff = tariffRepository.findById(changeTariffDto.getTariffId());
        if(tariff.isEmpty())
            throw new BadChangeTariffException("Тариф "+changeTariffDto.getTariffId()+" не найден!" );
        if (subscriberInfo.getTariff().getId().equals(changeTariffDto.getTariffId()))
            throw new BadChangeTariffException("Тариф для смены повторяет текущий тариф");
        subscriberInfo.setTariff(tariff.get());
        updateSubscriberInfo(subscriberInfo);

        jmsTemplate.convertAndSend("brt_sub_info_update", mapper.writeValueAsString(subscriberInfo));
        return subscriberInfo;
    }

    @Cacheable(value = "sub_info", key = "#phoneNumber")
    public SubscriberInfo findAbonent(String phoneNumber){
        return subscriberInfoRepository.findByNumber(phoneNumber);
    }

    @CachePut(value = "sub_info", key = "#subscriberInfo.number")
    public void updateSubscriberInfoFromBrt(SubscriberInfo subscriberInfo) {
        log.info("Получены данные абонента {} с сервиса brt!", subscriberInfo.getNumber());
    }

    @CachePut(value = "sub_info", key = "#subscriberInfo.number")
    public void updateSubscriberInfo(SubscriberInfo subscriberInfo) {
        subscriberInfoRepository.save(subscriberInfo);
    }


    public Abonent getAbonent(String username){
        Optional<Abonent> abonent = abonentRepository.findById(username.trim());
        if (abonent.isEmpty())
            throw new NotFoundAbonentException("Не найден абонент с данным username: "+username );
        return abonent.get();
    }
}

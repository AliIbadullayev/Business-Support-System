package org.billing.brt.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.billing.brt.exceptions.FurtherTarifficationException;
import org.billing.brt.exceptions.NotFoundReportException;
import org.billing.brt.pojo.CdrPlusLine;
import org.billing.brt.utils.BillingUtils;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.SubscriberInfo;
import org.billing.data.pojo.PhoneBalance;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.billing.data.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class BillingService {

    private final SubscriberInfoRepository subscriberInfoRepository;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper mapper;
    @Value("${hrs.server.host}")
    private String HRS_HOST;
    @Value("${hrs.server.port}")
    private String HRS_PORT;

    private RestTemplate restTemplate;

    public BillingService(SubscriberInfoRepository subscriberInfoRepository, JmsTemplate jmsTemplate, ObjectMapper mapper) {
        this.subscriberInfoRepository = subscriberInfoRepository;
        this.jmsTemplate = jmsTemplate;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
    }

    /* Передача следующему сервису BRT информацию про файл*/
    public PhoneBalanceDto furtherTariffication(File file) throws JsonProcessingException {
        String newUrl = CommonUtils.generateUrl(HRS_HOST, HRS_PORT, "api/hrs/tarifficate");
        HttpEntity<File> entity = new HttpEntity<>(file);
        PhoneBalanceDto phoneBalances = restTemplate.postForObject(newUrl, entity, PhoneBalanceDto.class);
        if (phoneBalances == null)
            throw new FurtherTarifficationException("С сервиса hrs не пришли никакие изменения баланса абонентов!");
        updateSubscriberInfo(phoneBalances);
        return phoneBalances;
    }

    public File validateCdr(File file){
        HashMap<String, List<CdrPlusLine>> phonePayloads = BillingUtils.parseFile(file);
        if (phonePayloads == null) throw new NotFoundReportException("После парсинга файла " + file.getAbsolutePath() + " не удалось найти репортов");

        try {
            Path newFolder = CommonUtils.getUserDirPath("/generated-files/cdr-plus");
            Files.createDirectories(newFolder);
            File cdrPlusFile = new File(newFolder.toFile(), CommonUtils.getDateInCdrFormat(Date.from(Instant.now())) +".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(cdrPlusFile));

            for (Map.Entry<String, List<CdrPlusLine>> entry: phonePayloads.entrySet()){
                SubscriberInfo sub = subscriberInfoRepository.findByNumber(entry.getKey());
                if (sub == null || sub.getMoney() <= 0) {
                    log.warn("Абонент с номером {} не прошел валидацию!", entry.getKey());
                    continue;
                }
                List<CdrPlusLine> list = entry.getValue().stream().sorted(Comparator.comparing(CdrPlusLine::getStartDate)).toList();
                for (CdrPlusLine line: list) {
                    line.setTariff(sub.getTariff().getId());
                    bw.write(line.toString());
                }
            }
            bw.close();
            return cdrPlusFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSubscriberInfo(PhoneBalanceDto phoneBalances) throws JsonProcessingException {
        for (PhoneBalance phoneBalance: phoneBalances.getPhoneBalances()){
            SubscriberInfo sub = findAbonent(phoneBalance.getPhoneNumber());
            sub.setMoney(sub.getMoney() - phoneBalance.getBalance());
            updateSubscriberInfo(sub);
            jmsTemplate.convertAndSend("crm", mapper.writeValueAsString(sub));
            phoneBalance.setBalance(sub.getMoney());
        }
        log.info("Данные абонентов успешно обновлены!");
    }

    @CachePut(value = "sub_info", key = "#subscriberInfo.number")
    public void updateSubscriberInfo(SubscriberInfo subscriberInfo) {
        subscriberInfoRepository.save(subscriberInfo);
    }

    @Cacheable(value = "sub_info", key = "#phoneNumber")
    public SubscriberInfo findAbonent(String phoneNumber){
        return subscriberInfoRepository.findByNumber(phoneNumber);
    }

    @CachePut(value = "sub_info", key = "#subscriberInfo.number")
    public void updateSubscriberInfoFromCrm(SubscriberInfo subscriberInfo) {
        log.info("Данные абонента {} успешно сохранены в кэш!", subscriberInfo.getNumber());
    }
}

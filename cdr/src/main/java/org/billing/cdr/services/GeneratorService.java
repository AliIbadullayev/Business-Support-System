package org.billing.cdr.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.billing.cdr.exceptions.GenerateCDRException;
import org.billing.cdr.pojo.CdrLine;
import org.billing.cdr.utils.CdrUtils;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.billing.data.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class GeneratorService {
    @Value("${brt.server.host}")
    private String BRT_HOST;
    @Value("${brt.server.port}")
    private String BRT_PORT;

    @Value("${generator.time.interval}")
    private int timeInterval;

    final private SubscriberInfoRepository subscriberInfoRepository;

    public GeneratorService(SubscriberInfoRepository subscriberInfoRepository) {
        this.subscriberInfoRepository = subscriberInfoRepository;
    }

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
    }

    /* Передача следующему сервису BRT информацию про файл*/
    public PhoneBalanceDto furtherTariffication(File file){
        String newUrl = CommonUtils.generateUrl(BRT_HOST, BRT_PORT, "api/brt/billing");
        HttpEntity<File> entity = new HttpEntity<>(file);
        return restTemplate.postForObject(newUrl, entity, PhoneBalanceDto.class);
    }

    public File generateCDRFile() throws IOException {
        if (timeInterval <= 60 ) throw new GenerateCDRException("Временной промежуток не может быть меньше минуты!");
        List<String> phones = subscriberInfoRepository.getRandomPhoneNumbers();
        Date reportBeginDateTime = Date.from(Instant.now());
        Date reportEndDateTime = Date.from(reportBeginDateTime.toInstant().plus(Duration.ofSeconds(timeInterval)));

        Path newFolder = CommonUtils.getUserDirPath("/generated-files/cdr");
        Files.createDirectories(newFolder);
        List<CdrLine> cdrLines = new ArrayList<>();

        File cdrFile = new File(newFolder.toFile(), "cdr_"+ CdrUtils.getDateInCdrFormat(reportBeginDateTime) +".txt");

        if (!cdrFile.exists() && cdrFile.createNewFile())
            log.info("Файл был успешно создан " + cdrFile.getPath());

        for (String phone: phones){
            double averageCallTimeSeconds = timeInterval * (0.02 + Math.random()/10);
            if (averageCallTimeSeconds > 5 * 60) averageCallTimeSeconds = (2 + Math.random()*4) * 60;
            int averageCallsInInterval = (int) (timeInterval / averageCallTimeSeconds);
            if (averageCallsInInterval > 100) averageCallsInInterval = (int) (80 + Math.random()*20);
            Date currentBeginDate = reportBeginDateTime;
            Date currentEndDate;
            List<String> callTypes = Arrays.asList("01", "02");
            for (int i = 0; i < averageCallsInInterval; i++){
                CdrLine line = new CdrLine();
                line.setPhoneNumber(phone);
                line.setCallType(callTypes.get(new Random().nextInt(callTypes.size())));
                line.setStartTime(currentBeginDate);
                double currentCallTimeMinutes = averageCallTimeSeconds* (1 - 0.1 + Math.random()*4/10);
                if(Date.from(reportBeginDateTime.toInstant().plus(Duration.ofSeconds((long) (currentCallTimeMinutes)))).after(reportEndDateTime)){
                    line.setEndTime(reportEndDateTime);
                    cdrLines.add(line);
                    break;
                }
                currentEndDate = Date.from(currentBeginDate.toInstant().plus(Duration.ofSeconds((long) (currentCallTimeMinutes))));
                line.setEndTime(currentEndDate);
                cdrLines.add(line);
                currentBeginDate = Date.from(currentEndDate.toInstant().plus(Duration.ofSeconds((long) (currentCallTimeMinutes * 0.1))));
            }
        }

        Collections.shuffle(cdrLines);
        BufferedWriter bw = new BufferedWriter(new FileWriter(cdrFile));
        for (CdrLine line: cdrLines){
            bw.write(line.toString());
        }
        bw.close();

        return cdrFile;
    }
}

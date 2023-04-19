package org.billing.cdr.services;

import org.billing.cdr.exceptions.GenerateCDRException;
import org.billing.cdr.pojo.CdrLine;
import org.billing.cdr.utils.CdrUtils;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GeneratorService {
    @Value("${generator.time.interval}")
    private int timeInterval;

    final private SubscriberInfoRepository subscriberInfoRepository;

    public GeneratorService(SubscriberInfoRepository subscriberInfoRepository) {
        this.subscriberInfoRepository = subscriberInfoRepository;
    }

    public File generateCDRFile() throws IOException {
        if (timeInterval <= 60 ) throw new GenerateCDRException("Временной промежуток не может быть меньше минуты!");
        List<String> phones = subscriberInfoRepository.getRandomPhoneNumbers();
        Date reportBeginDateTime = Date.from(Instant.now());
        Date reportEndDateTime = Date.from(reportBeginDateTime.toInstant().plus(Duration.ofSeconds(timeInterval)));

        Path source = Paths.get(this.getClass().getResource("/").getPath());
        Path newFolder = Paths.get(source.toAbsolutePath() + "/cdr/");
        Files.createDirectories(newFolder);
        List<CdrLine> cdrLines = new ArrayList<>();

        File cdrFile = new File(newFolder.toFile(), "cdr_"+ CdrUtils.getDateInCdrFormat(reportBeginDateTime) +".txt");
        if (!cdrFile.exists())
            cdrFile.createNewFile();
        for (String phone: phones){
            double averageCallTimeMinutes = timeInterval * (0.02 + Math.random()/10);
            int averageCallsInInterval = (int) (timeInterval / averageCallTimeMinutes);
            Date currentBeginDate = reportBeginDateTime;
            Date currentEndDate;
            List<String> callTypes = Arrays.asList("01", "02");
            for (int i = 0; i < averageCallsInInterval; i++){
                CdrLine line = new CdrLine();
                line.setPhoneNumber(phone);
                line.setCallType(callTypes.get(new Random().nextInt(callTypes.size())));
                line.setStartTime(currentBeginDate);
                double currentCallTimeMinutes = averageCallTimeMinutes* (1 - 0.1 + Math.random()*4/10);
                if(Date.from(reportBeginDateTime.toInstant().plus(Duration.ofSeconds((long) (currentCallTimeMinutes)))).after(reportEndDateTime)){
//                    Date lastEndTime = afterEndDate.getTime() - reportEndDateTime.getTime() - Date.from(Duration.ofSeconds((long) (currentCallTimeMinutes * 60)));
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

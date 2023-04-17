package org.billing.brt.utils;

import lombok.extern.slf4j.Slf4j;
import org.billing.data.models.Report;
import org.billing.data.pojo.Payload;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class BillingUtils {

    public static HashMap<String, Report> parseFile(File inputFile){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            HashMap<String, Report> reports = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                Report report = getPhoneReport(reports, line);
                Payload payload = getPayloadFromLine(line);
                List<Payload> payloads;
                if (report.getPayloads() == null) payloads = new ArrayList<>();
                else payloads = report.getPayloads();
                payloads.add(payload);
            }
            bufferedReader.close();
            log.info("Successfully parsed file: " + inputFile);
            return reports;
        } catch (IOException | ParseException e){
            log.warn("Cannot parse file because of: " + e.getMessage());
            return null;
        }
    }

    private static Payload getPayloadFromLine(String line) throws ParseException {
        Payload payload = new Payload();
        String[] strings = line.split(",\\s+");
        String callType = strings[0];
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss");
        Date startTime = parser.parse(strings[2]);
        Date endTime = parser.parse(strings[3]);

        payload.setStartTime(startTime);
        payload.setEndTime(endTime);
        payload.setCallType(callType);
        return payload;
    }

    private static Report getPhoneReport(HashMap<String, Report> reports, String line) {
        String[] strings = line.split(",\\s+");
        String phoneNumber = strings[1];
        if(!reports.containsKey(phoneNumber)) {
            Report report = new Report();
            report.setNumber(phoneNumber);
            report.setPayloads(new ArrayList<>());
            report.setCreationTime(Date.from(Instant.now()));
            reports.put(phoneNumber, report);
            return report;
        }
        return reports.get(phoneNumber);
    }
}

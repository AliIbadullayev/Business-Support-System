package org.billing.brt.utils;

import lombok.extern.slf4j.Slf4j;
import org.billing.brt.pojo.CdrPlusLine;

import java.io.*;
import java.text.ParseException;
import java.util.*;

@Slf4j
public class BillingUtils {

    public static HashMap<String, List<CdrPlusLine>> parseFile(File inputFile){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            HashMap<String, List<CdrPlusLine>> phonePayloads = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                CdrPlusLine payload = getPayloadFromLine(line);
                List<CdrPlusLine> payloads = phonePayloads.getOrDefault(payload.getNumber(), new ArrayList<>());
                payloads.add(payload);
                phonePayloads.put(payload.getNumber(), payloads);
            }
            bufferedReader.close();
            log.info("Successfully parsed file: " + inputFile);
            return phonePayloads;
        } catch (IOException | ParseException e){
            log.warn("Cannot parse file because of: " + e.getMessage());
            return null;
        }
    }

    private static CdrPlusLine getPayloadFromLine(String line) throws ParseException {
        CdrPlusLine cdrPlusLine = new CdrPlusLine();
        String[] strings = line.split(",\\s+");
        cdrPlusLine.setCallType(strings[0]);
        cdrPlusLine.setNumber(strings[1]);
        cdrPlusLine.setStartDate(strings[2]);
        cdrPlusLine.setEndDate(strings[3]);
        return cdrPlusLine;
    }
}

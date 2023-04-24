package org.billing.data.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.billing.data.models.Report;
import org.billing.data.pojo.Payload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
public class CommonUtils {

    public static String getDateInCdrFormat(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }

    static public String generateUrl(String destHost, String destPort, String destEndpoint){
        String newUrl = "http://" + destHost + ":" + destPort + "/" + destEndpoint;
        log.info("Запрос был успешно перенаправлен на URL: {}", newUrl);
        return newUrl;
    }

    /* Получение пути для сохранения файлов в необходимой директории текущему пути */
    static public Path getUserDirPath(String url){
        return Paths.get( new File(System.getProperty("user.dir")).getPath() + url);
    }

    @SneakyThrows
    static public int getMinutesFromPayload(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date duration = simpleDateFormat.parse(time);
        return duration.getHours() * 60 + duration.getMinutes() + (duration.getSeconds() > 0 ? 1 : 0);
    }

    @SneakyThrows
    static public Date getDurationFromPayload(Payload payload) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new Date(simpleDateFormat.parse(payload.getEndTime()).getTime() - simpleDateFormat.parse(payload.getStartTime()).getTime());
    }

    static public List<Report> getReports(File file) {
        HashMap<String, Report> reports = parseFile(file);
        if (reports == null)
            log.warn("После парсинга файла " + file.getAbsolutePath() + " не удалось найти репортов");
        return new ArrayList<>(Objects.requireNonNull(reports).values());
    }

    static public HashMap<String, Report> parseFile(File inputFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            HashMap<String, Report> reports = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
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
        } catch (IOException | ParseException e) {
            log.warn("Cannot parse file because of: " + e.getMessage());
            return null;
        }
    }

    static public Payload getPayloadFromLine(String line) throws ParseException {
        Payload payload = new Payload();
        String[] strings = line.split(",\\s+");
        String callType = strings[0];
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss");
        Date startTime = parser.parse(strings[2]);
        Date endTime = parser.parse(strings[3]);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        payload.setStartTime(formatter.format(startTime));
        payload.setEndTime(formatter.format(endTime));
        payload.setCallType(callType);
        return payload;
    }

    static public Report getPhoneReport(HashMap<String, Report> reports, String line) {
        String[] strings = line.split(",\\s+");
        String phoneNumber = strings[1];
        if (!reports.containsKey(phoneNumber)) {
            Report report = new Report();
            report.setTariff(strings[4]);
            report.setNumber(phoneNumber);
            report.setPayloads(new ArrayList<>());
            report.setCreationTime(Date.from(Instant.now()));
            reports.put(phoneNumber, report);
            return report;
        }
        return reports.get(phoneNumber);
    }
}

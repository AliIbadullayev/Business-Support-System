package org.billing.hrs.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.Report;
import org.billing.data.models.Tariff;
import org.billing.data.pojo.Payload;
import org.billing.data.pojo.PhoneBalance;
import org.billing.data.repositories.ReportRepository;
import org.billing.data.repositories.TariffRepository;
import org.billing.hrs.exceptions.NotFoundReportException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class HrsService {
    private final ReportRepository reportRepository;
    private final TariffRepository tariffRepository;


    public HrsService(ReportRepository reportRepository, TariffRepository tariffRepository) {
        this.reportRepository = reportRepository;
        this.tariffRepository = tariffRepository;
    }

    /* Происходит тарификация всех репортов поданных с сервиса BRT */
    public PhoneBalanceDto tarifficate(File file) {
        List<Report> reports = getReports(file);
        List<PhoneBalance> phoneBalances = new ArrayList<>();
        for (Report report : reports) {
            int totalTime = 0;
            float totalCost = 0;
            for (Payload payload : report.getPayloads()) {
                Payload tarifficatedPayload = tarifficatePayload(report, payload, totalTime);
                totalCost += tarifficatedPayload.getCost();
                totalTime += getMinutesFromPayload(tarifficatedPayload.getDuration());
            }
            totalCost += getTotalPrice(report.getTariffProxy(), totalTime);
            phoneBalances.add(new PhoneBalance(report.getNumber(), totalCost));
            report.setTotalCost(totalCost);
            report.setMonetaryUnit("Rubles");
            reportRepository.insert(report);
            log.info(String.valueOf(report));
        }
        return new PhoneBalanceDto(phoneBalances);
    }

    /* Проверка на то что, в тарифе "безлимит" (и похожих на него),
    пользователь, проговоривший менее 300 минут, обязан выплатить абонентскую плату
    */
    private float getTotalPrice(Tariff tariff, int totalTime) {
        if (tariff.getFixedPrice() != null && tariff.getFixedMinutes() != null) {
            if (totalTime <= tariff.getFixedMinutes())
                return tariff.getFixedPrice();
            else return 0;
        } else return 0;
    }

    /* Происходит тарификация единственной записи внутри репорта
    (возвращает объект записи с установленным временем разговора, и рассчитанной ценой за звонок)
    */
    private Payload tarifficatePayload(Report report, Payload payload, Integer totalTime) {
        Date duration = getDurationFromPayload(payload);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-0:00"));
        payload.setDuration(simpleDateFormat.format(duration));
        int minutes = getMinutesFromPayload(simpleDateFormat.format(duration));

        if (report.getTariffProxy().getFixedMinutes() != null && totalTime <= report.getTariffProxy().getFixedMinutes()) {
            if (report.getTariffProxy().getIsIncomingFree() &&  payload.getCallType().equals("02")) {
                payload.setCost(0F);
                return payload;
            }
            int leftMinutes = fillFixedTime(payload, report.getTariffProxy(), totalTime, minutes);
            if (leftMinutes != 0) {
                if (report.getTariffProxy().getNextTariffAfterFixedMinutes() != null) {
                    report.setTariffProxy(report.getTariffProxy().getNextTariffAfterFixedMinutes());
                }
                payload.setCost(payload.getCost() + report.getTariffProxy().getMinutePrice() * leftMinutes);
            }
        } else {
            if (report.getTariffProxy().getNextTariffAfterFixedMinutes() != null) {
                report.setTariffProxy(report.getTariffProxy().getNextTariffAfterFixedMinutes());
            }
            payload.setCost(report.getTariffProxy().getMinutePrice() * minutes);
        }

        return payload;
    }

    /*  Заполняет фиксированное время (например в тарифе безлимит это 300 минут), возвращает количество минут, которые не вместились в фиксированное время (также меняет содержимое payload) */
    private int fillFixedTime(Payload payload, Tariff tariff, int totalMinutes, int minutes) {
        int leftMinutes = minutes;
        if (totalMinutes + minutes > tariff.getFixedMinutes()) {
            leftMinutes = totalMinutes + minutes - tariff.getFixedMinutes();
            minutes = minutes - leftMinutes;
        }
        if (tariff.getFixedMinutePrice() == null) {
            payload.setCost(0F);
        } else {
            payload.setCost(tariff.getFixedMinutePrice() * minutes);
        }

        if (minutes == leftMinutes)
            return 0;
        return leftMinutes;
    }

    @SneakyThrows
    private int getMinutesFromPayload(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date duration = simpleDateFormat.parse(time);
        int minutes = duration.getHours() * 60 + duration.getMinutes() + (duration.getSeconds() > 0 ? 1 : 0);
        return minutes;
    }

    @SneakyThrows
    private Date getDurationFromPayload(Payload payload) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new Date(simpleDateFormat.parse(payload.getEndTime()).getTime() - simpleDateFormat.parse(payload.getStartTime()).getTime());
    }

    private List<Report> getReports(File file) {
        HashMap<String, Report> reports = parseFile(file);
        if (reports == null)
            throw new NotFoundReportException("После парсинга файла " + file.getAbsolutePath() + " не удалось найти репортов");
        return new ArrayList<>(reports.values());
    }

    private HashMap<String, Report> parseFile(File inputFile) {
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

    private Payload getPayloadFromLine(String line) throws ParseException {
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

    private Report getPhoneReport(HashMap<String, Report> reports, String line) {
        String[] strings = line.split(",\\s+");
        String phoneNumber = strings[1];
        Tariff tariff = tariffRepository.getReferenceById(strings[4]);
        if (!reports.containsKey(phoneNumber)) {
            Report report = new Report();
            report.setTariff(tariff.getId());
            report.setNumber(phoneNumber);
            report.setPayloads(new ArrayList<>());
            report.setCreationTime(Date.from(Instant.now()));
            report.setTariffProxy(tariff);
            reports.put(phoneNumber, report);
            return report;
        }
        return reports.get(phoneNumber);
    }
}

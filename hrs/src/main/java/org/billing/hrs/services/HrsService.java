package org.billing.hrs.services;

import lombok.extern.slf4j.Slf4j;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.Report;
import org.billing.data.models.Tariff;
import org.billing.data.pojo.Payload;
import org.billing.data.pojo.PhoneBalance;
import org.billing.data.repositories.ReportRepository;
import org.billing.data.repositories.TariffRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.billing.data.utils.CommonUtils.*;

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
            Tariff tariffProxy = tariffRepository.getReferenceById(report.getTariff());
            report.setTariffProxy(tariffProxy);
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
}

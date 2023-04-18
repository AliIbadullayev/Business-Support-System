package org.billing.hrs.services;

import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.Report;
import org.billing.data.models.Tariff;
import org.billing.data.pojo.Payload;
import org.billing.data.repositories.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class HrsService {
    private final ReportRepository reportRepository;

    public HrsService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /* Происходит тарификация всех репортов поданных с сервиса BRT */
    public List<PhoneBalanceDto> tarifficate(List<Report> reports){
        List<PhoneBalanceDto> phoneBalanceDtos = new ArrayList<>();
        for (Report report: reports){
            int totalTime = 0;
            float totalCost = 0;
            for (Payload payload: report.getPayloads()){
                Payload tarifficatedPayload = tarifficatePayload(report.getTariff(), payload, totalTime);
                totalCost += tarifficatedPayload.getCost();
                totalTime += getMinutesFromPayload(tarifficatedPayload.getDuration());
            }
            totalCost += getTotalPrice(report.getTariff(), totalTime);
            phoneBalanceDtos.add(new PhoneBalanceDto(report.getNumber(), totalCost));
            report.setTotalCost(totalCost);
            report.setMonetaryUnit("Rubles");
            reportRepository.insert(report);
        }
        return phoneBalanceDtos;
    }

    /* Проверка на то что, в тарифе "безлимит" (и похожих на него),
    пользователь, проговоривший менее 300 минут, обязан выплатить абонентскую плату
    */
    private float getTotalPrice(Tariff tariff, int totalTime){
        if (tariff.getFixedPrice() != null && tariff.getFixedMinutes() != null){
            if (totalTime <= tariff.getFixedMinutes())
                return tariff.getFixedPrice();
            else return 0;
        } else return 0;
    }

    /* Происходит тарификация единственной записи внутри репорта
    (возвращает объект записи с установленным временем разговора, и рассчитанной ценой за звонок)
    */
    private Payload tarifficatePayload(Tariff tariff, Payload payload, Integer totalTime){
        Date duration = getDurationFromPayload(payload);
        payload.setDuration(duration);
        int minutes = getMinutesFromPayload(duration);

        if (tariff.getIsIncomingFree() || tariff.getMinutePrice() == 0)
            payload.setCost(0F);

        if (tariff.getOtherOperatorTariff() != null){
//            some logic for change balance of other operator abonent
        }

        if (tariff.getFixedMinutes() != null) {
            int leftMinutes = fillFixedTime(payload, tariff, totalTime, minutes);
            if (leftMinutes != 0) {
                if (tariff.getNextTariffAfterFixedMinutes() != null) {
                    payload.setCost(payload.getCost() + tariff.getNextTariffAfterFixedMinutes().getMinutePrice() * leftMinutes);
                } else {
                    payload.setCost(payload.getCost() + tariff.getMinutePrice() * leftMinutes);
                }
            }
        } else {
            if (tariff.getNextTariffAfterFixedMinutes() != null) {
                payload.setCost(tariff.getNextTariffAfterFixedMinutes().getMinutePrice() * minutes);
            } else {
                payload.setCost(tariff.getMinutePrice() * minutes);
            }
        }

        return payload;
    }

/*  Заполняет фиксированное время (например в тарифе безлимит это 300 минут), возвращает количество минут, которые не вместились в фиксированное время (также меняет содержимое payload) */
    private int fillFixedTime (Payload payload, Tariff tariff, int totalMinutes, int minutes){
        int leftMinutes = minutes;
        if (totalMinutes + minutes > tariff.getFixedMinutes()){
            leftMinutes = totalMinutes + minutes - tariff.getFixedMinutes();
            minutes = minutes - leftMinutes;
        }
        if (tariff.getFixedMinutePrice() == null){
            payload.setCost(0F);
        }
        else {
            payload.setCost(tariff.getFixedMinutePrice() * minutes);
        }

        if (minutes == leftMinutes)
            return 0;
        return leftMinutes;
    }

    private int getMinutesFromPayload(Date duration) {
        int minutes = duration.getHours() * 60 + duration.getMinutes() + (duration.getSeconds() > 0 ? 1 : 0);
        return minutes;
    }

    private Date getDurationFromPayload(Payload payload){
        return new Date(payload.getEndTime().getTime() - payload.getStartTime().getTime());
    }
}

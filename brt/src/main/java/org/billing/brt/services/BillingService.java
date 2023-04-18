package org.billing.brt.services;

import lombok.extern.slf4j.Slf4j;
import org.billing.brt.exceptions.NotFoundReportException;
import org.billing.brt.exceptions.NotFoundSubscriberException;
import org.billing.brt.utils.BillingUtils;
import org.billing.data.dto.PhoneBalanceDto;
import org.billing.data.models.Report;
import org.billing.data.models.SubscriberInfo;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class BillingService {

    private final SubscriberInfoRepository subscriberInfoRepository;

    public BillingService(SubscriberInfoRepository subscriberInfoRepository) {
        this.subscriberInfoRepository = subscriberInfoRepository;
    }

    public List<Report> validateCdr(File file){
        HashMap<String, Report> reports = BillingUtils.parseFile(file);
        List<Report> cdrPlus = new ArrayList<>();
        if (reports == null) throw new NotFoundReportException("После парсинга файла " + file.getAbsolutePath() + " не удалось найти репортов");
        for (Report rep : reports.values()){
            SubscriberInfo sub = subscriberInfoRepository.findByNumber(rep.getNumber());
            if (sub == null) {
                reports.remove(rep.getNumber());
                throw new NotFoundSubscriberException("Данный номер " + rep.getNumber() + " не относится к оператору Ромашка");
            }
            if (sub.getMoney() <= 0){
                reports.remove(rep.getNumber());
                throw new NotFoundSubscriberException("На балансе номера " + sub.getNumber() + " не достаточно денег: " + sub.getMoney());
            }
            cdrPlus.add(rep);
        }
        return cdrPlus;
    }

    public List<PhoneBalanceDto> updateSubscriberInfo(List<PhoneBalanceDto> phoneBalances){
        for (PhoneBalanceDto phoneBalance: phoneBalances){
            SubscriberInfo sub = subscriberInfoRepository.findByNumber(phoneBalance.getPhoneNumber());
            sub.setMoney(sub.getMoney() - phoneBalance.getBalance());
            subscriberInfoRepository.save(sub);
        }
        log.info("Данные абонентов успешно обновлены!");
        return phoneBalances;
    }
}

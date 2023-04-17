package org.billing.crm.services;

import org.billing.crm.exception.BadAbonentAddException;
import org.billing.crm.exception.BadAbonentPayException;
import org.billing.crm.exception.BadChangeTariffException;
import org.billing.crm.exception.NotFoundAbonentException;
import org.billing.data.dto.AbonentAddDto;
import org.billing.data.dto.AbonentPayDto;
import org.billing.data.dto.ChangeTariffDto;
import org.billing.data.models.Abonent;
import org.billing.data.models.SubscriberInfo;
import org.billing.data.models.Tariff;
import org.billing.data.repositories.AbonentRepository;
import org.billing.data.repositories.SubscriberInfoRepository;
import org.billing.data.repositories.TariffRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AbonentService {
    private final AbonentRepository abonentRepository;
    private final SubscriberInfoRepository subscriberInfoRepository;
    private final TariffRepository tariffRepository;

    public AbonentService(AbonentRepository abonentRepository, SubscriberInfoRepository subscriberInfoRepository, TariffRepository tariffRepository) {
        this.abonentRepository = abonentRepository;
        this.subscriberInfoRepository = subscriberInfoRepository;
        this.tariffRepository = tariffRepository;
    }

    public SubscriberInfo replenishAbonentBalance( AbonentPayDto abonentPayDto){
        SubscriberInfo subscriberInfo = subscriberInfoRepository.findByNumber(abonentPayDto.getPhoneNumber());
        if(subscriberInfo == null)
            throw new NotFoundAbonentException("Не найден номер "+abonentPayDto.getPhoneNumber()+" по клиенту!");
        if (abonentPayDto.getMoney() <= 0F)
            throw new BadAbonentPayException("Сумма пополнения должна быть не меньше 0 рублей!");
        subscriberInfo.setMoney(subscriberInfo.getMoney()+abonentPayDto.getMoney());
        subscriberInfoRepository.save(subscriberInfo);
        return subscriberInfo;
    }

    public SubscriberInfo addAbonent(AbonentAddDto abonentAddDto){
        SubscriberInfo subscriberInfo = new SubscriberInfo();
        if (abonentAddDto.getTariffId() == null || abonentAddDto.getBalance() == null || abonentAddDto.getPhoneNumber() == null)
            throw new BadAbonentAddException("Пожалуйста перепроверьте введенные данные" + abonentAddDto);
        if (subscriberInfoRepository.findByNumber(abonentAddDto.getPhoneNumber()) != null)
            throw new BadAbonentAddException("Номер уже существует!");
        Optional<Tariff> tariff = tariffRepository.findById(abonentAddDto.getTariffId());
        if(tariff.isEmpty())
            throw new BadAbonentAddException("Тариф "+abonentAddDto.getTariffId()+" не найден!" );
        subscriberInfo.setTariff(tariff.get());
        if (abonentAddDto.getBalance() < 0)
            throw new BadAbonentAddException("Баланс нового пользователя не может быть отрицательным!");
        if (!abonentAddDto.getPhoneNumber().matches("^7[0-9]{10}\\b"))
            throw new BadAbonentAddException("Не корректный формат номера!");
        subscriberInfo.setMoney(abonentAddDto.getBalance());
        subscriberInfo.setNumber(abonentAddDto.getPhoneNumber());
        subscriberInfoRepository.save(subscriberInfo);
        return subscriberInfo;
    }

    public SubscriberInfo changeTariff(ChangeTariffDto changeTariffDto){
        SubscriberInfo subscriberInfo = subscriberInfoRepository.findByNumber(changeTariffDto.getPhoneNumber());
        System.out.println(changeTariffDto.getPhoneNumber());
        if (subscriberInfo == null)
            throw new BadChangeTariffException("Номера не действителен! Внесите номер в БД!");
        if (changeTariffDto.getTariffId() == null || changeTariffDto.getPhoneNumber() == null)
            throw new BadChangeTariffException("Пожалуйста перепроверьте введенные данные" + changeTariffDto);
        Optional<Tariff> tariff = tariffRepository.findById(changeTariffDto.getTariffId());
        if(tariff.isEmpty())
            throw new BadChangeTariffException("Тариф "+changeTariffDto.getTariffId()+" не найден!" );
        if (subscriberInfo.getTariff().getId().equals(changeTariffDto.getTariffId()))
            throw new BadChangeTariffException("Тариф для смены повторяет текущий тариф");
        subscriberInfo.setTariff(tariff.get());
        subscriberInfoRepository.save(subscriberInfo);
        return subscriberInfo;

    }

    public Abonent getAbonent(String username){
        Optional<Abonent> abonent = abonentRepository.findById(username.trim());
        if (abonent.isEmpty())
            throw new NotFoundAbonentException("Не найден абонент с данным username: "+username );
        return abonent.get();
    }
}

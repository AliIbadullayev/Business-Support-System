package org.billing.data.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tariffs")
@Data
public class Tariff {
    @Id
    private String id;

    private String name;

    /* Фиксированные минуты для оплаты (.. минут за 100 руб) */
    private Integer fixedMinutes;

    /* Плата за фиксированное время (300 минут за .. руб или 100 минут по .. руб/минута) */
    private Float fixedPrice;

    /*
    Плата за минуту по умолчанию или после истечения фиксированного срока
    (Каждая последующая минута - 1 руб)
    */
    private Float minutePrice;

    /* Является ли входящий звонок бесплатным */
    private Boolean isIncomingFree;

    /* Плата за минуту по фиксированной ставке (первые .. минут по 0,5 р/мин)  */
    private Float fixedMinutePrice;

    /* Плата по определенному тарифу после истечения фиксированного срока */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Tariff nextTariffAfterFixedMinutes;

    /* Тариф для пользователей других операторов */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Tariff otherOperatorTariff;
}

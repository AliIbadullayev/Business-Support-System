## Описание проекта
Ролевое веб-приложение имеющее разные узлы (микросервисы) нацеленное на тарификацию пользователей, получения отчетов выполнения тарификации, добавления пользователей, пополнение счета, изменения тарифа. 

### Использованные технологии 
#### СУБД: 
* `PostgreSQL` для хранения инф-ии об абонентах оператора Ромашка, тарифах (задание со звехдочкой) и пользовтелей сервиса (для авторизации)
* `MongoDB` для хранения отчетов в виде JSON файлов
#### Брокер сообщений:
* `Artemis` (activeMq) для отправки нотификаций на неоюходимые сервисы для синхронизации кэшей
#### Фреймворки: 
`Spring boot`, [`swagger UI doc`](https://github.com/AliIbadullayev/Business-Support-System/blob/main/assets/api-docs.yaml)

### Объяснение использованных подходов
* Было решено сделать БД с тарифами, при помощи параметров:
``` java
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
```
[`Модуль hrs`](https://github.com/AliIbadullayev/Business-Support-System/blob/ded7bfc88b60f198300d99342936f11d6876e121/hrs/src/main/java/org/billing/hrs/services/HrsService.java#L31) - где происходит тарификация 

[`Модуль data`](https://github.com/AliIbadullayev/Business-Support-System/tree/main/data/src/main) - так как в разных модулях нужен был доступ к одим и тем же СУБД, то было принято решение выделить общую логику в данный модуль 

[`Модуль brt`](https://github.com/AliIbadullayev/Business-Support-System/tree/main/brt/src/main) - где происходит валидация `cdr` файла и выдается файл `cdr+`

[`Модуль crm`](https://github.com/AliIbadullayev/Business-Support-System/tree/main/crm/src/main) - сервис на котором проиходит авторизация и который выступает в роли `API gateway`

[`Модуль cdr`](https://github.com/AliIbadullayev/Business-Support-System/tree/main/cdr/src/main) - сервис на котором генерируется `cdr` файл

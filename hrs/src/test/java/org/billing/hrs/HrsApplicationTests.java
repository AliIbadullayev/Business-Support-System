package org.billing.hrs;

import org.billing.data.repositories.ReportRepository;
import org.billing.hrs.services.HrsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
class HrsApplicationTests {
    @Autowired
    private HrsService service;
    @Autowired
    private ReportRepository repository;
    @Test
    @DisplayName("Проверка тарификации исходящего звонка длительностью 1 секунда на тарифе безлимит")
    void checkUnlimited1sec() throws IOException {
        File file = new File("/files/unlimited1sec.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(),(float) 300);
    }

    @Test
    @DisplayName("Проверка тарификации исходящих звонков длительностью 300 минут и 1 секунда на тарифе безлимит")
    void checkUnlimited300min1sec() throws IOException {
        File file = new File("/files/unlimited300min1sec.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(),(float) 101);
    }

    @Test
    @DisplayName("Проверка тарификации исходящих+входящих звонков общей длительностью 305 минут на тарифе безлимит")
    void checkUnlimited305IncomingOutgoing() throws IOException {
        File file = new File("/files/unlimited305minIncomingOutgoing.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(),(float) 105);
    }

    @Test
    @DisplayName("Проверка тарификации звонка длительностью 5 секунд на тарифе поминутный")
    void checkPerMinute5sec() throws IOException {
        File file = new File("/files/perminute5sec.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)1.5);
    }

    @Test
    @DisplayName("Проверка тарификации трёх звонков длительностью 5 секунд на тарифе поминутный")
    void checkPerMinute5sec3calls() throws IOException {
        File file = new File("/files/perminute5sec3calls.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)4.5);
    }

    @Test
    @DisplayName("Проверка тарификации только исходящие 99 минут 55 секунд по тарифу обычный")
    void checkOrdinary99min55sec() throws IOException {
        File file = new File("/files/ordinary99min55sec.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)50);
    }

    @Test
    @DisplayName("Проверка тарификации только исходящие 100 минут 55 секунд по тарифу обычный")
    void checkOrdinary100min55sec() throws IOException {
        File file = new File("/files/ordinary100min55sec.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)51.5);
    }

    @Test
    @DisplayName("Проверка тарификации только входящие 10 минут по тарифу обычный")
    void checkOrdinary10minIncoming() throws IOException {
        File file = new File("/files/ordinary10minIncoming.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)0);
    }

    @Test
    @DisplayName("Проверка тарификации входящие и исходящие по 10 минут по тарифу обычный")
    void checkOrdinary10minIncomingOutgoing() throws IOException {
        File file = new File("/files/ordinary10minIncomingOutgoing.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)5);
    }

    @Test
    @DisplayName("Проверка тарификации безлимитный 105 минут + поминутный 15 минут")
    void checkUnlimited105Perminute15() throws IOException {
        File file = new File("/files/unlimited105perminute15.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)122.5);
    }

    @Test
    @DisplayName("Проверка тарификации безлимитный 105 минут + обычный 110 минут")
    void checkUnlimited105Ordinary110() throws IOException {
        File file = new File("/files/unlimited105ordinary110.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)165);
    }

    @Test
    @DisplayName("Проверка тарификации поминутный 10 минут + обычный 110 минут")
    void checkPerminute10Ordinary110() throws IOException {
        File file = new File("/files/perminute10ordinary110.txt");
        var result = service.tarifficate(file);
        Assertions.assertEquals(result.getPhoneBalances().get(0).getBalance(), (float)80);
    }
}

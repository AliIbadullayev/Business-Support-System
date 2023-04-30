package org.billing.cdr;

import org.billing.cdr.services.GeneratorService;
import org.billing.data.pojo.Payload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.security.core.parameters.P;
import org.springframework.util.Assert;

import javax.xml.crypto.Data;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class CdrApplicationTests {

    @Autowired
    private GeneratorService service;

    @Test
    @DisplayName("Проверка создания файла с CDR записями")
    void contextLoads() throws IOException {
        File file = service.generateCDRFile();
        Assertions.assertNotEquals(file,null);
    }

    @Test
    @DisplayName("Проверка формата CDR записей")
    void checkFormatCDR() throws IOException {
        File inputFile = service.generateCDRFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        int countLine = 0;
        int correctCDRLine = 0;
        while ((line = bufferedReader.readLine()) != null){
            countLine++;
            if(Pattern.matches("\\d{2}, \\d+, \\d{14}, \\d{14}", line))
                correctCDRLine++;
        }
        Assertions.assertEquals(countLine, correctCDRLine);
    }

    @Test
    @DisplayName("Проверка типа вызова")
    void checkCallType() throws IOException {
        File inputFile = service.generateCDRFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        int countLine = 0;
        int correctTypeLine = 0;
        Pattern pattern = Pattern.compile("(\\d{2}), (\\d+), (\\d{14}), (\\d{14})");
        while ((line = bufferedReader.readLine()) != null){
            countLine++;
            Matcher matcher = pattern.matcher(line);
            matcher.matches();
            if(matcher.group(1).equals("01") || matcher.group(1).equals("02"))
                correctTypeLine++;
        }
        Assertions.assertEquals(countLine, correctTypeLine);
    }


    @Test
    @DisplayName("Проверка валидности даты")
    void checkDateAndTime() throws IOException, ParseException {
        File inputFile = service.generateCDRFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        Pattern pattern = Pattern.compile("(\\d{2}), (\\d+), (\\d{14}), (\\d{14})");
        while ((line = bufferedReader.readLine()) != null) {
            SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");
            formater.setLenient(false);
            Matcher matcher = pattern.matcher(line);
            matcher.matches();
            Date start = formater.parse(matcher.group(3));
            Date end = formater.parse(matcher.group(4));
        }
    }

    @Test
    @DisplayName("Проверка начала и окончания звонка")
    void checkStartAndStop() throws IOException, ParseException {
        File inputFile = service.generateCDRFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line;
        int countLine = 0;
        int correctTime = 0;
        Pattern pattern = Pattern.compile("(\\d{2}), (\\d+), (\\d{14}), (\\d{14})");
        while ((line = bufferedReader.readLine()) != null) {
            countLine++;
            SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");
            formater.setLenient(false);
            Matcher matcher = pattern.matcher(line);
            matcher.matches();
            Date start = formater.parse(matcher.group(3));
            Date end = formater.parse(matcher.group(4));
            if (start.before(end))
                correctTime++;
        }
        Assertions.assertEquals(countLine, correctTime);
    }
}

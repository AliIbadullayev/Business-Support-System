package org.billing.data.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class CommonUtils {
    public static String getDateInCdrFormat(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }

    static public String generateUrl(String destHost, String destPort, String destEndpoint){
        String newUrl = "http://" + destHost + ":" + destPort + "/" + destEndpoint;
        log.info("Request will be sent to transaction service on URL: {}", newUrl);
        return newUrl;
    }

    /* Получение пути для сохранения файлов в необходимой директории текущему пути */
    static public Path getUserDirPath(String url){
        return Paths.get( new File(System.getProperty("user.dir")).getPath() + url);
    }
}

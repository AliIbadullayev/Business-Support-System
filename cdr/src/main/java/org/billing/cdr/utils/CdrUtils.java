package org.billing.cdr.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CdrUtils {
    public static String getDateInCdrFormat(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }
}

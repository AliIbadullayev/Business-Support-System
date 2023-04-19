package org.billing.cdr.pojo;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class CdrLine {
    private String callType;
    private String phoneNumber;
    private Date startTime;
    private Date endTime;

    public String toString(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return callType+", "+phoneNumber+ ", " +simpleDateFormat.format(startTime)+ ", " +simpleDateFormat.format(endTime) + "\n";
    }
}

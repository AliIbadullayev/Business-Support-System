package org.billing.data.pojo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

@Document
@Data
public class Payload {
    private String callType;
    private String startTime;
    private String endTime;
    private String duration;
    private Float cost;

}

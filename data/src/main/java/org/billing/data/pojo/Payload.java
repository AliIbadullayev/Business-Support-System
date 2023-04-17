package org.billing.data.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.billing.data.IsoDateDeserializer;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Payload {
    private String callType;
    private Date startTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date endTime;
    @DateTimeFormat(pattern = "hh:MM:ss")
    private Date duration;
    private Float cost;
}

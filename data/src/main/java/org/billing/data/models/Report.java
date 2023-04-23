package org.billing.data.models;

import lombok.Data;
import lombok.ToString;
import org.billing.data.pojo.Payload;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Document
@Data
@ToString
public class Report {
    @MongoId
    private String id;
    private List<Payload> payloads;
    private String number;
    @Transient
    private Tariff tariffProxy;
    private String tariff;
    private Float totalCost;
    private String monetaryUnit;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationTime = Date.from(Instant.now());
}

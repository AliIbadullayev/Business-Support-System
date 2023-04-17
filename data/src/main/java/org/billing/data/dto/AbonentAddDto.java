package org.billing.data.dto;

import lombok.Data;

@Data
public class AbonentAddDto {
    private String phoneNumber;
    private String tariffId;
    private Float balance;
}

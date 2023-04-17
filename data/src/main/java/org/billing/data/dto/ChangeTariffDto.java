package org.billing.data.dto;

import lombok.Data;

@Data
public class ChangeTariffDto {
    private String phoneNumber;
    private String tariffId;
}

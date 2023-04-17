package org.billing.brt.dto;

import lombok.Data;

@Data
public class PhoneBalanceDto {
    private String phoneNumber;
    private Float balance;
}

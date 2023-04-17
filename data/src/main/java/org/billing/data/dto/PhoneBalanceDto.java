package org.billing.data.dto;

import lombok.Data;

@Data
public class PhoneBalanceDto {
    private String phoneNumber;
    private Float balance;
}

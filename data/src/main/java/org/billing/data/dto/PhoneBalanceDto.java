package org.billing.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PhoneBalanceDto {
    private String phoneNumber;
    private Float balance;
}

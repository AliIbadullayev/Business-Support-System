package org.billing.data.dto;

import lombok.*;
import org.billing.data.pojo.PhoneBalance;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneBalanceDto {
    private List<PhoneBalance> phoneBalances;
}

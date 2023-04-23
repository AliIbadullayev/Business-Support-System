package org.billing.data.pojo;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PhoneBalance {
    private String phoneNumber;
    private Float balance;
}

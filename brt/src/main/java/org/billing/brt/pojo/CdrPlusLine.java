package org.billing.brt.pojo;

import lombok.Data;

@Data
public class CdrPlusLine {
    private String callType;
    private String number;
    private String startDate;
    private String endDate;
    private String tariff;

    @Override
    public String toString() {
        return callType + ", " + number + ", " + startDate + ", " + endDate + ", " + tariff + "\n";
    }
}

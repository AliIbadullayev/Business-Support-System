package org.billing.data.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "sub_info")
@Data
public class SubscriberInfo {
    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Long subId;

    @Column(unique = true)
    @Pattern(regexp = "^7[0-9]{10}\\b", message = "The number format is incorrect!\nIt must be so: 79999999999")
    private String number;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "tariff")
    private Tariff tariff;

    private Float money;
}

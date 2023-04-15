package org.billing.data.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "clients")
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    @Column(columnDefinition = "varchar(255)")
    private String name;

    @Column(columnDefinition = "varchar(255)")
    private String surname;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="client")
    private List<SubscriberInfo> subscriberInfos;
}

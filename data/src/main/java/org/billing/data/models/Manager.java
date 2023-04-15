package org.billing.data.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "managers")
@Data
public class Manager {
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
}

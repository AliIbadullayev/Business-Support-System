package org.billing.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "abonents")
@Data
public class Abonent {

    @Id
    @Column(name = "username")
    private String username;

    @JsonIgnore
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Column(columnDefinition = "varchar(255)")
    private String name;

    @Column(columnDefinition = "varchar(255)")
    private String surname;
}

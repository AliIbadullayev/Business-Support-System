package org.billing.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private String username;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
}

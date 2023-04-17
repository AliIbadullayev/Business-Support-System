package org.billing.data.repositories;

import org.billing.data.models.Abonent;
import org.billing.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbonentRepository extends JpaRepository<Abonent, String> {
    public Abonent findByUser(User username);
}

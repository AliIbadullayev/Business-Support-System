package org.billing.data.repositories;

import org.billing.data.models.Tariff;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, String> {
}

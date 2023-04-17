package org.billing.data.repositories;

import org.billing.data.models.SubscriberInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberInfoRepository extends JpaRepository<SubscriberInfo, Long> {
    public SubscriberInfo findByNumber(String phoneNumber);
}

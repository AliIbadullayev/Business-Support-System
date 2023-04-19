package org.billing.data.repositories;

import org.billing.data.models.SubscriberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriberInfoRepository extends JpaRepository<SubscriberInfo, Long> {
    public SubscriberInfo findByNumber(String phoneNumber);

    @Query(
            value = "select number from sub_info where random() < 0.2",
            nativeQuery = true)
    public List<String> getRandomPhoneNumbers();
}

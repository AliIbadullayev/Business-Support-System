package org.billing.data.repositories;

import org.billing.data.models.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
    Report getFirstByNumberOrderByCreationTimeDesc(String phone);
}

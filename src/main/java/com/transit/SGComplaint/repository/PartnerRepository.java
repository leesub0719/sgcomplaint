package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PartnerRepository extends
        JpaRepository<Partner, Long>,
        JpaSpecificationExecutor<Partner> {

    long countByStatus(String status);
}

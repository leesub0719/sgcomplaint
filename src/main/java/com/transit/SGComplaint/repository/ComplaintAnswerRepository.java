package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.ComplaintAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ComplaintAnswerRepository
        extends JpaRepository<ComplaintAnswer, Long> {

    Optional<ComplaintAnswer> findByComplaintNo(Long complaintNo);

    List<ComplaintAnswer> findByComplaintNoIn(Collection<Long> complaintNumbers);
}

package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, Long> {

    List<ComplaintAttachment> findByComplaintNoInOrderByAttachmentNoAsc(
            List<Long> complaintNumbers);
}

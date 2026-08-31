package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.ComplaintAnswerAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintAnswerAttachmentRepository
        extends JpaRepository<ComplaintAnswerAttachment, Long> {

    List<ComplaintAnswerAttachment> findByAnswerNoInOrderByAnswerAttachmentNoAsc(
            List<Long> answerNumbers);

    long countByAnswerNo(Long answerNo);
}

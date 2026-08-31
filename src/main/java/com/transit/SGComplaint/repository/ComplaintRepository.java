package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.Complaint;
import com.transit.SGComplaint.domain.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByEmpNoAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long empNo,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    List<Complaint> findTop5ByOrderByCreatedAtDesc();

    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Complaint> findByStatusOrderByCreatedAtDesc(
            ComplaintStatus status,
            Pageable pageable);

    long countByStatus(ComplaintStatus status);

    @Query("""
            select complaint from Complaint complaint
             where (:category = '' or complaint.category = :category)
               and (:keyword = '' or lower(complaint.title) like lower(concat('%', :keyword, '%')))
             order by complaint.createdAt desc
            """)
    Page<Complaint> searchPublicComplaints(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);
}

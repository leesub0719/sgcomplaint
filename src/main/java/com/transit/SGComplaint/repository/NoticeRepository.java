package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select notice from Notice notice
             where (:keyword = '' or lower(notice.title) like lower(concat('%', :keyword, '%')))
             order by notice.pinned desc, notice.createdAt desc
            """)
    Page<Notice> searchNotices(@Param("keyword") String keyword, Pageable pageable);

    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Notice> findTop3ByOrderByPinnedDescCreatedAtDesc();

    List<Notice> findByPopupOrderByCreatedAtDesc(String popup);
}

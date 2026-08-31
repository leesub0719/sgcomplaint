package com.transit.SGComplaint.repository;

import com.transit.SGComplaint.domain.MainBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MainBannerRepository extends JpaRepository<MainBanner, Long> {

    List<MainBanner> findAllByOrderByDisplayOrderAscBannerNoAsc();

    Optional<MainBanner> findTopByOrderByDisplayOrderDescBannerNoDesc();
}

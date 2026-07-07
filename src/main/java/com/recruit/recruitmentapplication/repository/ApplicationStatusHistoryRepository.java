package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    Optional<ApplicationStatusHistory> findFirstByApplication_IdOrderByChangedAtDesc(Long applicationId);
}

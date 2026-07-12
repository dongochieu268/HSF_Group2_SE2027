package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    Optional<ApplicationStatusHistory> findFirstByApplication_IdOrderByChangedAtDesc(Long applicationId);
    @EntityGraph(attributePaths = "changedBy")
    List<ApplicationStatusHistory> findByApplication_IdOrderByChangedAtDesc(Long applicationId);
}

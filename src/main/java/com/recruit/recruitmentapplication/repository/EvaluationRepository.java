package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Evaluation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByInterview_Id(Long interviewId);

    boolean existsByInterview_Id(Long interviewId);

    // SCR-17: "Evaluation summary" - danh sách đánh giá đã nộp cho một đơn ứng tuyển
    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.interviewer
            JOIN FETCH e.interview iv
            WHERE iv.application.id = :applicationId
            ORDER BY e.submittedAt DESC
            """)
    List<Evaluation> findByApplicationId(@Param("applicationId") Long applicationId);
}

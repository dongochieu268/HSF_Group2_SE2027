package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Evaluation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByInterview_Id(Long interviewId);

    boolean existsByInterview_Id(Long interviewId);
}

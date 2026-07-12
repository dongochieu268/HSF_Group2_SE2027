package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.ApplicationNote;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {
    @EntityGraph(attributePaths = "author")
    List<ApplicationNote> findByApplication_IdOrderByCreatedAtDesc(Long applicationId);
}

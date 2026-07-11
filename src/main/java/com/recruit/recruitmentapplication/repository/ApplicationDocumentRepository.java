package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.ApplicationDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {
    List<ApplicationDocument> findByApplication_IdOrderByUploadedAtDesc(Long applicationId);
}

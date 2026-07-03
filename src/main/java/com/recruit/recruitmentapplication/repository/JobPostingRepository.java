package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.JobPosting;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatus(JobPosting.PostingStatus status);

    List<JobPosting> findByJobType(JobPosting.JobType jobType);

    List<JobPosting> findByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company "
            + "WHERE jp.status = 'ACTIVE' AND LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "ORDER BY jp.postedDate DESC")
    List<JobPosting> findOpenJobsByTitle(@Param("keyword") String keyword);

    List<JobPosting> findByLocationContainingIgnoreCase(String location);

    List<JobPosting> findByCompany_Id(Long companyId);

    List<JobPosting> findByCompany_Name(String companyName);

    List<JobPosting> findBySalaryMinGreaterThanEqual(BigDecimal minSalary);

    List<JobPosting> findByDeadlineAfter(LocalDate date);

    List<JobPosting> findByStatusAndJobType(JobPosting.PostingStatus status, JobPosting.JobType type);

    long countByStatus(JobPosting.PostingStatus status);

    long countByCompany_Id(Long companyId);

    boolean existsByTitleAndCompany_Name(String title, String companyName);

    boolean existsByTitleAndCompany_Id(String title, Long companyId);

    Optional<JobPosting> findByTitleAndCompany_Name(String title, String companyName);

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company WHERE jp.status = 'ACTIVE' ORDER BY jp.postedDate DESC")
    List<JobPosting> findOpenJobsWithCompany();

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company LEFT JOIN FETCH jp.createdBy WHERE jp.id = :id")
    Optional<JobPosting> findByIdWithCompany(@Param("id") Long id);

    @Query("SELECT DISTINCT jp FROM JobPosting jp JOIN FETCH jp.company LEFT JOIN FETCH jp.createdBy "
            + "LEFT JOIN FETCH jp.requiredSkills WHERE jp.id = :id")
    Optional<JobPosting> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT jp FROM JobPosting jp
            JOIN FETCH jp.company
            LEFT JOIN FETCH jp.createdBy
            LEFT JOIN FETCH jp.requiredSkills
            WHERE jp.id = :id AND jp.status = 'ACTIVE'
            """)
    Optional<JobPosting> findActiveByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT jp FROM JobPosting jp LEFT JOIN FETCH jp.requiredSkills ORDER BY jp.id")
    List<JobPosting> findAllWithRequiredSkills();

    @Query("""
            SELECT jp FROM JobPosting jp
            JOIN FETCH jp.company
            LEFT JOIN FETCH jp.createdBy
            WHERE (:ownerId IS NULL OR jp.createdBy.id = :ownerId)
              AND (:status IS NULL OR jp.status = :status)
              AND (:department IS NULL OR jp.department = :department)
              AND (:keyword IS NULL OR LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY jp.postedDate DESC, jp.id DESC
            """)
    List<JobPosting> findManagedJobsFiltered(@Param("ownerId") Long ownerId,
                                              @Param("status") JobPosting.PostingStatus status,
                                              @Param("department") String department,
                                              @Param("keyword") String keyword);

    @Query("SELECT DISTINCT jp.department FROM JobPosting jp "
            + "WHERE (:ownerId IS NULL OR jp.createdBy.id = :ownerId) ORDER BY jp.department")
    List<String> findManagedDepartments(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.status = :status "
            + "AND (:ownerId IS NULL OR jp.createdBy.id = :ownerId)")
    long countManagedByStatus(@Param("status") JobPosting.PostingStatus status, @Param("ownerId") Long ownerId);

    @Query("SELECT DISTINCT jp FROM JobPosting jp JOIN jp.requiredSkills s WHERE s.name = :skillName")
    List<JobPosting> findByRequiredSkillName(@Param("skillName") String skillName);

    @Query("""
            SELECT DISTINCT jp FROM JobPosting jp
            JOIN jp.requiredSkills s
            WHERE s.name = :skillName AND jp.status = 'ACTIVE'
            """)
    List<JobPosting> findOpenJobsBySkill(@Param("skillName") String skillName);

    @Query("""
            SELECT jp FROM JobPosting jp
            WHERE jp.salaryMin >= :min AND jp.salaryMax <= :max AND jp.status = 'ACTIVE'
            """)
    List<JobPosting> findByOpenSalaryRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query(value = """
            SELECT jp.* FROM job_postings jp
            JOIN (
                SELECT a.job_posting_id, COUNT(a.id) AS application_count
                FROM applications a
                GROUP BY a.job_posting_id
                ORDER BY application_count DESC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
            ) ranked ON jp.id = ranked.job_posting_id
            ORDER BY ranked.application_count DESC
            """, nativeQuery = true)
    List<JobPosting> findMostAppliedJobs(@Param("limit") int limit);

    @Query(value = """
            SELECT jp.* FROM job_postings jp
            WHERE jp.posted_date >= DATEADD(day, (:days * -1), CAST(CURRENT_TIMESTAMP AS date))
              AND jp.status = 'ACTIVE'
            """, nativeQuery = true)
    List<JobPosting> findRecentOpenJobs(@Param("days") int days);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE JobPosting jp SET jp.status = 'CLOSED' WHERE jp.deadline < :today AND jp.status = 'ACTIVE'")
    int closeExpiredPostings(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE JobPosting jp SET jp.status = :status WHERE jp.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") JobPosting.PostingStatus status);
}

package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // Lấy 10 hoạt động gần nhất, sắp xếp theo thời gian mới nhất
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();

    // Lấy toàn bộ nhật ký, mới nhất trước - dùng cho trang danh sách đầy đủ
    List<ActivityLog> findAllByOrderByCreatedAtDesc();
}
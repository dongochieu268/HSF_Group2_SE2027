package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.entity.ActivityLog;
import com.recruit.recruitmentapplication.entity.ActivityLog.ActivityEventType;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ActivityLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void log(ActivityEventType eventType, User actor, String description, String ipAddress) {
        activityLogRepository.save(new ActivityLog(eventType, actor, description, ipAddress));
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> findAll() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc();
    }
}

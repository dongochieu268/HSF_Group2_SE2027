package com.recruit.recruitmentapplication.entity;

import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JobPostingStatusConverter implements AttributeConverter<PostingStatus, String> {
    @Override
    public String convertToDatabaseColumn(PostingStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public PostingStatus convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("OPEN".equals(normalized)) {
            return PostingStatus.ACTIVE;
        }
        return PostingStatus.valueOf(normalized);
    }
}

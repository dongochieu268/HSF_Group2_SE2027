package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPostingStatusConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobPostingStatusConverterTests {
    private final JobPostingStatusConverter converter = new JobPostingStatusConverter();

    @Test
    void readsLegacyOpenStatusAsActive() {
        assertEquals(JobPosting.PostingStatus.ACTIVE, converter.convertToEntityAttribute("OPEN"));
    }
}

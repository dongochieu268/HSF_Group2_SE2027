package com.recruit.recruitmentapplication.dto;

public class PipelineStageSummary {
    private final String name;
    private final long count;
    private final int percentage;

    public PipelineStageSummary(String name, long count, int percentage) {
        this.name = name;
        this.count = count;
        this.percentage = percentage;
    }

    public String getName() { return name; }
    public long getCount() { return count; }
    public int getPercentage() { return percentage; }
}

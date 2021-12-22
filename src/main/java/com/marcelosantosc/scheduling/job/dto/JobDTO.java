package com.marcelosantosc.scheduling.job.dto;

import com.marcelosantosc.scheduling.job.model.Job;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class JobDTO {

    private Long id;

    private String description;

    private String deadlineForExecution;

    private Integer estimatedTime;

    public Job toJobEntity() {
        return new Job(id, description, LocalDateTime.parse(deadlineForExecution), Duration.ofHours(estimatedTime));
    }
}

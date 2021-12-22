package com.marcelosantosc.scheduling.job.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class Job {

    private Long id;

    private String description;

    private LocalDateTime deadlineForExecution;

    private Integer estimatedTime;


}

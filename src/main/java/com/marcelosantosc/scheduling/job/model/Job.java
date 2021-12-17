package com.marcelosantosc.scheduling.job.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Getter
public class Job {

    @EqualsAndHashCode.Include()
    private Long id;

    private String description;

    private LocalDateTime deadlineForExecution;

    private Duration estimatedTime;

}

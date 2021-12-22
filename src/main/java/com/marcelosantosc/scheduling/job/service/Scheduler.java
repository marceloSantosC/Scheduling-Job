package com.marcelosantosc.scheduling.job.service;

import com.marcelosantosc.scheduling.job.model.Job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;

public interface Scheduler {

    List<Queue<Job>> schedule(List<Job> jobs, LocalDateTime executionWindowStart, LocalDateTime executionWindowEnd);
}

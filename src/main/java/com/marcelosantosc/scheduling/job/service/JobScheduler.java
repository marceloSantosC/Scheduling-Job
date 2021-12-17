package com.marcelosantosc.scheduling.job.service;

import com.marcelosantosc.scheduling.job.model.Job;

import java.util.*;
import java.util.stream.Collectors;

public class JobScheduler {

    public Queue<Job> sortJobsByDate(List<Job> jobs) {
        return jobs.stream()
                .sorted(Comparator.comparing(Job::getDeadlineForExecution))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}

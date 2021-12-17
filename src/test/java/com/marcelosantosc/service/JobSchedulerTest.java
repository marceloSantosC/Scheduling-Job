package com.marcelosantosc.service;

import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.service.JobScheduler;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JobSchedulerTest {

    private final JobScheduler jobScheduler;

    public JobSchedulerTest() {
        this.jobScheduler = new JobScheduler();
    }

    @Test
    void should_sort_jobs_by_date() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), Duration.ofHours(2));
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), Duration.ofHours(4));
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), Duration.ofHours(6));
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), Duration.ofHours(8));

        List<Job> sortedList = new LinkedList<>();
        sortedList.add(firstJob);
        sortedList.add(secondJob);
        sortedList.add(thirdJob);
        sortedList.add(lastJob);

        List<Job> jobsToSort = new ArrayList<>(sortedList);
        Collections.shuffle(jobsToSort);

        assertNotEquals(sortedList, jobsToSort);

        Queue<Job> jobsSortedByAlgorithm = jobScheduler.sortJobsByDate(jobsToSort);

        assertEquals(sortedList, jobsSortedByAlgorithm);
    }

    @Test
    void should_not_sort_jobs_by_date() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), Duration.ofHours(2));
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), Duration.ofHours(4));
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), Duration.ofHours(6));
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), Duration.ofHours(8));

        List<Job> sortedList = new LinkedList<>();
        sortedList.add(firstJob);
        sortedList.add(secondJob);
        sortedList.add(thirdJob);
        sortedList.add(lastJob);

        List<Job> jobsToSort = new ArrayList<>(sortedList);
        Collections.shuffle(jobsToSort);

        Queue<Job> jobsSortedByAlgorithm = jobScheduler.sortJobsByDate(jobsToSort);

        assertEquals(sortedList, jobsSortedByAlgorithm);
    }


    private Job newJob(LocalDateTime deadline, Duration estimatedTime) {
        return Job.builder().id(new Random().nextLong())
                .deadlineForExecution(deadline)
                .estimatedTime(estimatedTime)
                .description("Teste")
                .build();

    }
}

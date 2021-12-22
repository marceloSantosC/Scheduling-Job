package com.marcelosantosc.service;

import com.marcelosantosc.scheduling.job.exception.ValidationException;
import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.model.ValidationResult;
import com.marcelosantosc.scheduling.job.service.JobScheduler;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerTest {

    private final JobScheduler scheduler;

    public JobSchedulerTest() {
        this.scheduler = new JobScheduler();
    }

    @Test
    void should_group_jobs_with_maximum_8_hours_of_estimated_time_per_group_while_respecting_deadline() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 4);
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), 6);
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), 8);

        LocalDateTime executionWindowStart = LocalDateTime.parse("2021-10-12T10:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2021-12-15T14:30:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob, lastJob);
        List<Queue<Job>> scheduledJobs = scheduler.schedule(jobs, executionWindowStart, executionWindowEnd);

        List<Job> firstJobGroup = Arrays.asList(firstJob, secondJob);
        List<Job> secondJobGroup = Collections.singletonList(thirdJob);
        List<Job> thirdJobGroup = Collections.singletonList(lastJob);

        assertFalse(scheduledJobs.isEmpty());
        assertEquals(firstJobGroup, scheduledJobs.get(0));
        assertEquals(secondJobGroup, scheduledJobs.get(1));
        assertEquals(thirdJobGroup, scheduledJobs.get(2));
    }

    @Test
    void should_throw_validation_exception_with_invalid_jobs() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 4);
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), 6);
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), 8);

        LocalDateTime executionWindowStart = LocalDateTime.parse("2020-10-12T10:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2020-12-15T14:30:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob, lastJob);

        assertThrows(ValidationException.class, () -> scheduler.schedule(jobs, executionWindowStart, executionWindowEnd));
    }


    @Test
    void should_sort_jobs_by_date() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 4);
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), 6);
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), 8);

        List<Job> sortedList = Arrays.asList(firstJob, secondJob, thirdJob, lastJob);
        List<Job> jobsToSort = Arrays.asList(secondJob, lastJob, firstJob, thirdJob);


        Method method = ReflectionUtils.findMethod(JobScheduler.class, "sortJobsByDate", List.class).get();
        Queue<Job> jobsSortedByAlgorithm = (Queue<Job>) ReflectionUtils.invokeMethod(method, scheduler, jobsToSort);

        assertEquals(sortedList, jobsSortedByAlgorithm);
    }

    @Test
    void should_not_return_validation_errors_with_valid_jobs() {
        Job firstJob = newJob(LocalDateTime.parse("2021-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 4);
        Job thirdJob = newJob(LocalDateTime.parse("2021-11-25T14:15:00"), 6);
        Job lastJob =  newJob(LocalDateTime.parse("2021-12-15T14:30:00"), 8);

        LocalDateTime start = LocalDateTime.parse("2021-10-12T10:20:30");
        LocalDateTime end = LocalDateTime.parse("2021-12-15T14:30:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob, lastJob);

        Method method = ReflectionUtils.findMethod(JobScheduler.class, "validateJobs",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        ValidationResult validationResult = (ValidationResult) ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getMessages().isEmpty());
    }

    @Test
    void should_return_false_when_all_jobs_are_inside_execution_window() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 1);
        Job secondJob = newJob(LocalDateTime.parse("2020-10-12T12:30:00"), 1);
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T00:20:30"), 1);

        LocalDateTime start = LocalDateTime.parse("2020-10-12T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2020-10-12T23:59:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "areThereJobsOutOfTheExecutionWindow",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        boolean areThereJobsOutOfTheExecutionWindow = (boolean) ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertFalse(areThereJobsOutOfTheExecutionWindow);
    }

    @Test
    void should_return_true_when_there_are_jobs_after_the_execution_window() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 1);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 1);
        Job thirdJob = newJob(LocalDateTime.parse("2019-10-12T00:20:30"), 1);

        LocalDateTime start = LocalDateTime.parse("2019-10-12T00:20:30");
        LocalDateTime end = LocalDateTime.parse("2020-10-12T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "areThereJobsOutOfTheExecutionWindow",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        boolean areThereJobsOutOfTheExecutionWindow = (boolean) ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertTrue(areThereJobsOutOfTheExecutionWindow);
    }

    @Test
    void should_return_true_when_there_are_jobs_before_the_execution_window() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 1);
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), 1);
        Job thirdJob = newJob(LocalDateTime.parse("2019-10-12T00:20:30"), 1);

        LocalDateTime start = LocalDateTime.parse("2022-12-17T00:20:30");
        LocalDateTime end = LocalDateTime.parse("2022-12-17T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "areThereJobsOutOfTheExecutionWindow",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        boolean areThereJobsOutOfTheExecutionWindow = (boolean) ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertTrue(areThereJobsOutOfTheExecutionWindow);
    }

    @Test
    void should_return_false_when_all_jobs_estimated_time_is_less_than_or_equal_8() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), 6);
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), 8);

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "isJobDurationGreaterThanMax", List.class).get();
        boolean isJobDurationGreaterThanMax = (boolean) ReflectionUtils.invokeMethod(method, scheduler, jobs);

        assertFalse(isJobDurationGreaterThanMax);
    }

    @Test
    void should_return_true_when_there_are_jobs_with_estimated_time_greater_than_8() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 2);
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), 6);
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), 9);

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "isJobDurationGreaterThanMax", List.class).get();
        boolean isJobDurationGreaterThanMax = (boolean) ReflectionUtils.invokeMethod(method, scheduler, jobs);

        assertTrue(isJobDurationGreaterThanMax);
    }

    @Test
    void should_return_false_when_execution_duration_is_not_greater_than_execution_window_duration() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 1);
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), 1);
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), 1);

        LocalDateTime start = LocalDateTime.parse("2022-12-17T00:20:30");
        LocalDateTime end = LocalDateTime.parse("2022-12-17T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "executionTimeGreaterThanJobWindow",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        boolean executionTimeGreaterThanJobWindow = (boolean)
                ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertFalse(executionTimeGreaterThanJobWindow);
    }

    @Test
    void should_return_true_when_execution_duration_is_greater_than_execution_window_duration() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), 8);
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), 8);
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), 8);

        LocalDateTime start = LocalDateTime.parse("2022-12-17T00:20:30");
        LocalDateTime end = LocalDateTime.parse("2022-12-17T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        Method method = ReflectionUtils.findMethod(JobScheduler.class, "executionTimeGreaterThanJobWindow",
                List.class, LocalDateTime.class, LocalDateTime.class).get();
        boolean executionTimeGreaterThanJobWindow = (boolean)
                ReflectionUtils.invokeMethod(method, scheduler, jobs, start, end);

        assertTrue(executionTimeGreaterThanJobWindow);
    }


    private Job newJob(LocalDateTime deadline, Integer estimatedTime) {
        return Job.builder().id(new Random().nextLong())
                .deadlineForExecution(deadline)
                .estimatedTime(estimatedTime)
                .description("Teste")
                .build();

    }
}

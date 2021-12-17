package com.marcelosantosc.service;

import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.model.ValidationResult;
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
    void should_not_return_validation_errors_with_valid_jobs() {
        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), Duration.ofHours(2));
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), Duration.ofHours(6));
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), Duration.ofHours(8));

        LocalDateTime executionWindowStart = LocalDateTime.parse("2020-10-12T10:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2020-10-20T11:20:30");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);

        ValidationResult validationResult = jobScheduler.validateJobs(executionWindowStart, executionWindowEnd, jobs);

        assertTrue(validationResult.isValid());
        assertTrue(validationResult.getMessages().isEmpty());
    }

    @Test
    void list_with_jobs_after_execution_window_end_should_be_invalid() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), Duration.ofHours(1));
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), Duration.ofHours(1));
        Job thirdJob = newJob(LocalDateTime.parse("2019-10-12T00:20:30"), Duration.ofHours(1));

        LocalDateTime executionWindowStart = LocalDateTime.parse("2019-10-12T00:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2020-10-12T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);

        ValidationResult validationResult = jobScheduler.validateJobs(executionWindowStart, executionWindowEnd, jobs);

        assertFalse(validationResult.isValid());
        assertEquals( 1, validationResult.getMessages().size());
        assertEquals(validationResult.getMessages().get(0), "Existem jobs com o prazo de execução fora janela de execução");
    }

    @Test
    void list_with_jobs_before_execution_window_end_should_be_invalid() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), Duration.ofHours(1));
        Job secondJob = newJob(LocalDateTime.parse("2021-10-13T12:30:00"), Duration.ofHours(1));
        Job thirdJob = newJob(LocalDateTime.parse("2019-10-12T00:20:30"), Duration.ofHours(1));

        LocalDateTime executionWindowStart = LocalDateTime.parse("2022-12-17T00:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2022-12-17T14:20:00");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        ValidationResult validationResult = jobScheduler.validateJobs(executionWindowStart, executionWindowEnd, jobs);

        assertFalse(validationResult.isValid());
        assertEquals( 1, validationResult.getMessages().size());
        assertEquals("Existem jobs com o prazo de execução fora janela de execução", validationResult.getMessages().get(0));
    }

    @Test
    void jobs_with_duration_greather_than_8_should_be_invalid() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), Duration.ofHours(2));
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), Duration.ofHours(6));
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), Duration.ofHours(9));

        LocalDateTime executionWindowStart = LocalDateTime.parse("2020-10-12T10:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2020-10-20T11:20:30");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        ValidationResult validationResult = jobScheduler.validateJobs(executionWindowStart, executionWindowEnd, jobs);

        assertFalse(validationResult.isValid());
        assertEquals( 1, validationResult.getMessages().size());
        assertEquals("Existem jobs com mais de 8 horas de tempo estimado", validationResult.getMessages().get(0));
    }

    @Test
    void total_execution_duration_should_be_greather_than_execution_window_duration() {

        Job firstJob = newJob(LocalDateTime.parse("2020-10-12T10:20:30"), Duration.ofHours(8));
        Job secondJob = newJob(LocalDateTime.parse("2020-10-13T12:30:00"), Duration.ofHours(8));
        Job thirdJob = newJob(LocalDateTime.parse("2020-10-12T11:20:30"), Duration.ofHours(8));

        LocalDateTime executionWindowStart = LocalDateTime.parse("2020-10-12T10:20:30");
        LocalDateTime executionWindowEnd = LocalDateTime.parse("2020-10-12T11:20:30");

        List<Job> jobs = Arrays.asList(firstJob, secondJob, thirdJob);
        ValidationResult validationResult = jobScheduler.validateJobs(executionWindowStart, executionWindowEnd, jobs);

        assertFalse(validationResult.isValid());
        assertEquals( 1, validationResult.getMessages().size());
        assertEquals("O tempo de execução das jobs é maior do que a duração da janela de execução", validationResult.getMessages().get(0));
    }


    private Job newJob(LocalDateTime deadline, Duration estimatedTime) {
        return Job.builder().id(new Random().nextLong())
                .deadlineForExecution(deadline)
                .estimatedTime(estimatedTime)
                .description("Teste")
                .build();

    }
}

package com.marcelosantosc.scheduling.job.service;

import com.marcelosantosc.scheduling.job.exception.ValidationException;
import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
public class JobScheduler implements Scheduler {

    private static final Integer MAX_QUEUE_DURATION = 8;

    public List<Queue<Job>> schedule(List<Job> jobs,
                                     LocalDateTime executionWindowStart,
                                     LocalDateTime executionWindowEnd) {

        ValidationResult validationResult = validateJobs(jobs, executionWindowStart, executionWindowEnd);
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult);
        }

        Queue<Job> jobsSortedByDate = sortJobsByDate(jobs);
        List<Queue<Job>> jobGroups = new LinkedList<>();

        Queue<Job> jobQueue = new LinkedList<>();
        Integer currentQueueDuration = 0;
        jobGroups.add(jobQueue);


        while (!jobsSortedByDate.isEmpty()) {
            Job job = jobsSortedByDate.poll();
            currentQueueDuration = currentQueueDuration + job.getEstimatedTime();

            if (currentQueueDuration > MAX_QUEUE_DURATION) {
                jobQueue = new LinkedList<>();
                jobGroups.add(jobQueue);
                jobQueue.add(job);
                currentQueueDuration = job.getEstimatedTime();
            } else {
                jobQueue.add(job);
            }
        }
        return jobGroups;
    }

    private ValidationResult validateJobs(List<Job> jobs, LocalDateTime windowStart, LocalDateTime windowEnd) {

        ValidationResult validationResult = new ValidationResult();
        validationResult.addValidation(!isJobDurationGreaterThanMax(jobs), "Existem jobs com mais de 8 horas de tempo estimado");
        validationResult.addValidation(!areThereJobsOutOfTheExecutionWindow(jobs, windowStart, windowEnd),
                "Existem jobs com o prazo de execução fora janela de execução");
        validationResult.addValidation(!executionTimeGreaterThanJobWindow(jobs, windowStart, windowEnd),
                "O tempo de execução das jobs é maior do que a duração da janela de execução");

        return validationResult;
    }

    private boolean isJobDurationGreaterThanMax(List<Job> jobs) {
        return jobs.stream().anyMatch(job -> job.getEstimatedTime().compareTo((MAX_QUEUE_DURATION)) > 0);

    }

    private boolean areThereJobsOutOfTheExecutionWindow(List<Job> jobs, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return jobs.stream().anyMatch(job ->
                job.getDeadlineForExecution().isAfter(windowEnd) || job.getDeadlineForExecution().isBefore(windowStart));
    }

    private boolean executionTimeGreaterThanJobWindow(List<Job> jobs, LocalDateTime windowStart, LocalDateTime windowEnd) {
        Integer executionDuration = jobs.stream().map(Job::getEstimatedTime).reduce(0, Integer::sum);
        return windowStart.plus(Duration.ofHours(executionDuration)).isAfter(windowEnd);
    }

    private Queue<Job> sortJobsByDate(List<Job> jobs) {
        return jobs.stream()
                .sorted(Comparator.comparing(Job::getDeadlineForExecution))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}

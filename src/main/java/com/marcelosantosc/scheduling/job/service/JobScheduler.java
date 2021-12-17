package com.marcelosantosc.scheduling.job.service;

import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.model.ValidationResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JobScheduler {

    private static final Duration MAX_JOB_DURATION = Duration.ofHours(8);


    public ValidationResult validateJobs(LocalDateTime windowStart,
                                         LocalDateTime windowEnd,
                                         List<Job> jobs) {

        ValidationResult validationResult = new ValidationResult();
        validationResult.addValidation(!isJobDurationGreaterThanMax(jobs), "Existem jobs com mais de 8 horas de tempo estimado");
        validationResult.addValidation(!areThereJobsOutOfTheExecutionWindow(jobs, windowStart, windowEnd),
                "Existem jobs com o prazo de execução fora janela de execução");
        validationResult.addValidation(!executionTimeGreaterThanJobWindow(jobs, windowStart, windowEnd),
                "O tempo de execução das jobs é maior do que a duração da janela de execução");

        return validationResult;
    }

    private boolean isJobDurationGreaterThanMax(List<Job> jobs) {
        return jobs.stream().anyMatch(job -> job.getEstimatedTime().compareTo((MAX_JOB_DURATION)) > 0);

    }

    private boolean areThereJobsOutOfTheExecutionWindow(List<Job> jobs, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return jobs.stream().anyMatch(job ->
                job.getDeadlineForExecution().isAfter(windowEnd) || job.getDeadlineForExecution().isBefore(windowStart));
    }

    private boolean executionTimeGreaterThanJobWindow(List<Job> jobs, LocalDateTime windowStart, LocalDateTime windowEnd) {
        Duration executionDuration = jobs.stream().map(Job::getEstimatedTime).reduce(Duration.ZERO, Duration::plus);
        return windowStart.plus(executionDuration).isAfter(windowEnd);
    }

    public LinkedList<Job> sortJobsByDate(List<Job> jobs) {
        return jobs.stream()
                .sorted(Comparator.comparing(Job::getDeadlineForExecution))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}

package com.marcelosantosc.scheduling.job.controller;

import com.marcelosantosc.scheduling.job.dto.JobDTO;
import com.marcelosantosc.scheduling.job.model.Job;
import com.marcelosantosc.scheduling.job.service.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/scheduler", produces = MediaType.APPLICATION_JSON_VALUE)
public class SchedulerController {

    private final Scheduler scheduler;

    public SchedulerController(@Autowired Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PutMapping(value = "/schedule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Queue<Job>> schedule(@RequestBody List<JobDTO> jobs,
                                     @RequestParam String start,
                                     @RequestParam String end) {

        List<Job> jobEntities = jobs.stream().map(JobDTO::toJobEntity).collect(Collectors.toList());
        return scheduler.schedule(jobEntities, LocalDateTime.parse(start), LocalDateTime.parse(end));
    }


}

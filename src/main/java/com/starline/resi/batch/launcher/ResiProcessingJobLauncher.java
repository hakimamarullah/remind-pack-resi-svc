package com.starline.resi.batch.launcher;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResiProcessingJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job resiProcessingJob;
    private final MeterRegistry meterRegistry;

    @Scheduled(cron = "${cron.resi-processing-job:* * */3 * * *}", zone = "Asia/Jakarta")
    @CacheEvict(value = "resi", allEntries = true)
    public void launchResiProcessingJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(resiProcessingJob, jobParameters);
            log.info("Job launched with execution ID: {}", jobExecution.getId());
            meterRegistry.counter("resi.job.launched").increment();

        } catch (Exception e) {
            log.error("Failed to launch resi processing job: {}", e.getMessage(), e);
            meterRegistry.counter("resi.job.launch_failed").increment();
        }
    }
}

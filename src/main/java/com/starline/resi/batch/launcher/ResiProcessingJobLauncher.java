package com.starline.resi.batch.launcher;

import com.starline.resi.batch.config.BatchInstanceId;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResiProcessingJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job resiProcessingJob;
    private final MeterRegistry meterRegistry;
    private final BatchInstanceId instanceId;



    @Scheduled(fixedDelay = 4, timeUnit = TimeUnit.HOURS) // 4 hours
    public void launchResiProcessingJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("cutoffTime", LocalDateTime.now().minusHours(4).toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("instanceId", instanceId.getBatchIntanceId())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(resiProcessingJob, jobParameters);

            log.info("Job launched with execution ID: {}", jobExecution.getId());

            // Record metrics
            meterRegistry.counter("resi.job.launched").increment();

        } catch (Exception e) {
            log.error("Failed to launch resi processing job: {}", e.getMessage(), e);
            meterRegistry.counter("resi.job.launch_failed").increment();
        }
    }
}

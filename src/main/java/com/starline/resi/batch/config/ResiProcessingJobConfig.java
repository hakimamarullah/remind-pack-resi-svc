package com.starline.resi.batch.config;

import com.starline.resi.batch.processor.ResiItemProcessor;
import com.starline.resi.batch.reader.ResiItemReader;
import com.starline.resi.batch.writer.ResiItemWriter;
import com.starline.resi.dto.ResiUpdateResult;
import com.starline.resi.dto.projection.ResiProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@Slf4j
public class ResiProcessingJobConfig {

    @Bean
    public Job resiProcessingJob(JobRepository jobRepository,
                                 Step resiProcessingStep,
                                 JobExecutionListener jobExecutionListener) {
        return new JobBuilder("resiProcessingJob", jobRepository)
                .listener(jobExecutionListener)
                .start(resiProcessingStep)
                .build();
    }

    @Bean
    public Step resiProcessingStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ResiItemReader reader,
                                   ResiItemProcessor processor,
                                   ResiItemWriter writer) {
        return new StepBuilder("resiProcessingStep", jobRepository)
                .<ResiProjection, ResiUpdateResult>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(100)
                .skip(Exception.class)
                .listener(stepExecutionListener())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("resi-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting step: {} with parameters: {}",
                        stepExecution.getStepName(),
                        stepExecution.getJobParameters());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Step {} completed. Read: {}, Processed: {}, Written: {}, Skipped: {}, Failed: {}",
                        stepExecution.getStepName(),
                        stepExecution.getReadCount(),
                        stepExecution.getSummary(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount(),
                        stepExecution.getFailureExceptions().size());
                return ExitStatus.COMPLETED;
            }
        };
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("Starting job: {} with parameters: {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                var startTime = Optional.ofNullable(jobExecution.getStartTime()).orElse(LocalDateTime.now());
                log.info("Job {} completed with status: {}. Duration: {}ms",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStatus(),
                        Duration.between(startTime, jobExecution.getEndTime()).toMillis());
            }
        };
    }
}


package com.starline.resi.batch.config;

import com.starline.resi.dto.resi.ResiUpdateResult;
import com.starline.resi.model.Resi;
import io.opentelemetry.context.Context;
import jakarta.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ResiProcessingJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final ItemProcessor<Resi, ResiUpdateResult> resiItemProcessor;
    private final ItemWriter<ResiUpdateResult> resiItemJpaWriter;

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
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("resiProcessingStep", jobRepository)
                .<Resi, ResiUpdateResult>chunk(1000, transactionManager)
                .reader(resiJpaReader())
                .processor(resiItemProcessor)
                .writer(resiItemJpaWriter)
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
    @StepScope
    public JpaPagingItemReader<Resi> resiJpaReader() {
        JpaPagingItemReader<Resi> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT r FROM Resi r WHERE r.subscriptionExpiryDate > :cutOffDate");
        reader.setParameterValues(Map.of("cutOffDate", LocalDateTime.now()));
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(500); // read in chunks
        reader.setSaveState(false);
        reader.setName("resiJpaReader");
        return reader;
    }


    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("resi-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return task -> Context.taskWrapping(executor).execute(task);
    }

    @Bean
    public StepExecutionListener stepExecutionListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(@NonNull StepExecution stepExecution) {
                log.info("Starting step: {}", stepExecution.getStepName());
            }

            @Override
            public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
                log.info("Step {} completed. Read: {}, Written: {}, Skipped: {}, Failed: {}",
                        stepExecution.getStepName(),
                        stepExecution.getReadCount(),
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
            public void beforeJob(@NonNull JobExecution jobExecution) {
                log.info("Starting job: {} with parameters: {}",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters());
            }

            @Override
            public void afterJob(@NonNull JobExecution jobExecution) {
                var startTime = Optional.ofNullable(jobExecution.getStartTime()).orElse(LocalDateTime.now());
                log.info("Job {} completed with status: {}. Duration: {}ms",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStatus(),
                        Duration.between(startTime, jobExecution.getEndTime()).toMillis());
            }
        };
    }
}

package com.hqp.batch.config;

import com.hqp.batch.entity.db.JpaUser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * <p>JPA批处理配置.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Configuration
public class JpaBatchConfig {

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    @Autowired
    public JpaBatchConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean
    public ItemReader<JpaUser> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<JpaUser> itemReader = new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        itemReader.setQueryString("select u from JpaUser u");
        return itemReader;
    }

    @Bean
    public ItemProcessor<JpaUser, JpaUser> processor() {
        return item -> {
            item.setAge(item.getAge() + 1);
            item.setDescription("have deal");
            return item;
        };
    }

    @Bean
    public ItemWriter<JpaUser> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<JpaUser> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }

    @Bean
    public Step step(ItemReader<JpaUser> jpaItemReader,
                     ItemProcessor<JpaUser, JpaUser> processor,
                     ItemWriter<JpaUser> jpaItemWriter) {
        return stepBuilderFactory.get("addAge")
                .<JpaUser, JpaUser>chunk(2)
                .reader(jpaItemReader)
                .processor(processor)
                .writer(jpaItemWriter)
                .build();
    }

    @Bean
    public Job job(Step step) {
        return jobBuilderFactory.get("addJob")
                .listener(new JobExecutionListener() {
                    private Long time;

                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        time = System.currentTimeMillis();
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        System.out.println(String.format("任务耗时：%sms", System.currentTimeMillis() - time));
                    }
                })
                .flow(step)
                .end()
                .build();
    }
}

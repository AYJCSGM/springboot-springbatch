package com.hqp.batch.config;

import com.hqp.batch.entity.db.MonthBill;
import com.hqp.batch.entity.db.UserAccount;
import com.hqp.batch.entity.db.WaterRecord;
import com.hqp.batch.exception.MoneyNotEnoughException;
import com.hqp.batch.repository.MonthBillRepository;
import com.hqp.batch.repository.UserAccountRepository;
import com.hqp.batch.repository.WaterRecordRepository;
import com.hqp.batch.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <p>Step Flow批处理配置.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Configuration
public class FlowBatchConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowBatchConfig.class);

    private EntityManagerFactory entityManagerFactory;

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    public FlowBatchConfig(EntityManagerFactory entityManagerFactory, StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    /*
     * 生成月账单
     */
    @Bean
    public Step generateBillStep(WaterRecordRepository waterRecordRepository) {
        return stepBuilderFactory.get("generateBillStep")
                .<WaterRecord, MonthBill>chunk(10)
                .reader(new JpaPagingItemReader<WaterRecord>() {{
                    setQueryString("select w from WaterRecord w");
                    setEntityManagerFactory(entityManagerFactory);
                }})
                .processor((ItemProcessor<WaterRecord, MonthBill>) item ->
                {
                    if (item.getIsGenerateBill()) {
                        // 已生成的不会生成月账单
                        return null;
                    } else {
                        MonthBill monthBill = new MonthBill();
                        // 计费策略
                        monthBill.setTotalFee(item.getConsumption().multiply(BigDecimal.valueOf(1.5d)));
                        monthBill.setIsPaid(false);
                        monthBill.setIsNotice(false);
                        monthBill.setCreateTime(new Date());
                        monthBill.setUserId(item.getUserId());
                        item.setIsGenerateBill(true);
                        waterRecordRepository.save(item);
                        return monthBill;
                    }
                })
                .writer(new JpaItemWriter<MonthBill>() {{
                    setEntityManagerFactory(entityManagerFactory);
                }})
                .build();
    }

    /*
     * 自动缴费
     */
    @Bean
    public Step deductStep(MonthBillRepository monthBillRepository, UserAccountRepository userAccountRepository) {
        return stepBuilderFactory.get("deductStep")
                .<MonthBill, UserAccount>chunk(10)
                .faultTolerant()
                // 允许忽略的异常，否则会以Job Failed结束
                .skip(MoneyNotEnoughException.class)
                // 允许最大跳过100个余额不足数据
                .skipLimit(100)
                .reader(new JpaPagingItemReader<MonthBill>() {{
                    setQueryString("select m from MonthBill m");
                    setEntityManagerFactory(entityManagerFactory);
                }})
                .processor((ItemProcessor<MonthBill, UserAccount>) item -> {
                    if (item.getIsPaid() || item.getIsNotice()) {
                        // 过滤已缴费
                        return null;
                    }
                    // 根据账单信息查找账户信息
                    Optional<UserAccount> accountOptional = userAccountRepository.findById(item.getUserId());
                    if (accountOptional.isPresent()) {
                        UserAccount userAccount = accountOptional.get();
                        // 自动缴费
                        if (userAccount.getAutoDeduct()) {
                            // 余额充足
                            if (userAccount.getAccountBalance().compareTo(item.getTotalFee()) > -1) {
                                userAccount.setAccountBalance(userAccount.getAccountBalance().subtract(item.getTotalFee()));
                                item.setIsPaid(true);
                                item.setIsNotice(true);
                            } else {
                                // 余额不足
                                throw new MoneyNotEnoughException();
                            }
                        } else {
                            item.setIsNotice(true);
                            // 手动缴费——短信通知
                            System.out.println(String.format("Message sent to UserID %s ——> your water bill this month is %s￥",
                                    item.getUserId(), item.getTotalFee()));
                        }
                        monthBillRepository.save(item);
                        return userAccount;
                    } else {
                        // 账户不存在
                        LOGGER.error(String.format("账单号 %s,用户ID %s,的用户不存在", item.getId(), item.getUserId()));
                        return null;
                    }
                })
                .writer(new JpaItemWriter<UserAccount>() {{
                    setEntityManagerFactory(entityManagerFactory);
                }})
                .build();
    }

    /*
     * 余额不足通知
     */
    @Bean
    public Step noticeStep(MonthBillRepository monthBillRepository) {
        return stepBuilderFactory.get("noticeStep")
                .tasklet((contribution, chunkContext) -> {
                    List<MonthBill> monthBills = monthBillRepository.findUnpaidMonthBill(
                            DateUtils.getBeginDayOfMonth(), DateUtils.getEndDayOfMonth());
                    for (MonthBill monthBill : monthBills) {
                        System.out.println(
                                String.format("Message sent to UserID %s ——> your water bill this month is ￥%s，please pay for it",
                                        monthBill.getUserId(), monthBill.getTotalFee()));
                    }
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job flowJob(Step generateBillStep, Step deductStep, Step noticeStep) {
        return jobBuilderFactory.get("flowJob")
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
                .flow(generateBillStep)
                .next(deductStep)
                .next((jobExecution, stepExecution) -> {
                    if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED) &&
                            stepExecution.getSkipCount() > 0) {
                        return new FlowExecutionStatus("NOTICE USER");
                    } else {
                        return new FlowExecutionStatus(stepExecution.getExitStatus().toString());
                    }
                })
                .on("COMPLETED").end()
                .on("NOTICE USER").to(noticeStep)
                .end()
                .build();
    }
}
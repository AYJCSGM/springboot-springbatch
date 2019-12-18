package com.hqp.batch.repository;


import com.hqp.batch.entity.db.MonthBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * <p>Repository——月账单.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Repository
public interface MonthBillRepository extends JpaRepository<MonthBill, Integer> {

    /**
     * 根据条件筛选本月未缴且未通知的账单
     *
     * @param start 扫描起始时间
     * @param end   扫描结束时间
     * @return List<MonthBill>
     */
    @Query("select m from MonthBill m where m.isNotice = false and m.isPaid = false and m.createTime between ?1 and ?2")
    List<MonthBill> findUnpaidMonthBill(Date start, Date end);
}

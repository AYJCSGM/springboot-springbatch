package com.hqp.batch.entity.db;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>Flow-月账单.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Entity
@Table(name = "month_bill")
@Data
public class MonthBill {

    @Id
    @GeneratedValue
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 总费用
     */
    private BigDecimal totalFee;

    /**
     * 是否已缴
     */
    private Boolean isPaid;

    /**
     * 是否通知
     */
    private Boolean isNotice;

    /**
     * 账单生成时间
     */
    private Date createTime;
}

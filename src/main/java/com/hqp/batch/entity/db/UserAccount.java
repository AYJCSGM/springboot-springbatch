package com.hqp.batch.entity.db;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * <p>Flow-用户账户.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Data
@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue
    private Integer id;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 账户余额
     */
    private BigDecimal accountBalance;
    /**
     * 是否开启自动扣减
     */
    private Boolean autoDeduct;
}

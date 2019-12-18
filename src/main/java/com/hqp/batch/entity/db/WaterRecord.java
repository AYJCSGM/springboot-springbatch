package com.hqp.batch.entity.db;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * <p>Flow-水表记录.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Data
@Entity
@Table(name = "water_record")
public class WaterRecord {

    @Id
    @GeneratedValue
    private Integer id;
    /**
     * 用户Id
     */
    private Integer userId;
    /**
     * 用水量
     */
    private BigDecimal consumption;
    /**
     * 是否生成账单
     */
    private Boolean isGenerateBill;
}

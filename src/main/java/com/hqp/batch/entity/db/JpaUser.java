package com.hqp.batch.entity.db;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <p>Jpa使用.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Data
@Entity
@Table(name = "batch_user")
public class JpaUser implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private Integer age;

    private String description;
}

package com.hqp.batch.repository;


import com.hqp.batch.entity.db.WaterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Repository——水表记录.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Repository
public interface WaterRecordRepository extends JpaRepository<WaterRecord, Integer> {
}

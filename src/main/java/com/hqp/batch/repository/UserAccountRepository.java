package com.hqp.batch.repository;


import com.hqp.batch.entity.db.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Repository——用户账户.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

}

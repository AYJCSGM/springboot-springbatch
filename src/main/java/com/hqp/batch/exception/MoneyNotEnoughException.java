package com.hqp.batch.exception;

/**
 * <p>异常-余额不足.<br>
 *
 * @author 黄秋平
 * @version 2019年12月3日
 */
public class MoneyNotEnoughException extends Exception {

    public MoneyNotEnoughException() {
    }

    public MoneyNotEnoughException(String message) {
        super(message);
    }

    public MoneyNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }

    public MoneyNotEnoughException(Throwable cause) {
        super(cause);
    }
}

package com.bot.commom.exception;

/**
 * @author murongyehua
 * @version 1.0 2020/9/23
 */
public class BotException extends RuntimeException {

    private static final long serialVersionUID = -5504765167300324431L;

    public BotException() {
        super();
    }

    public BotException(String exceptionMsg) {
        super(exceptionMsg);
    }

}

package org.gamein.marketservergamein2022.core.exception;

public class NotEnoughMoneyException extends Exception{
    public NotEnoughMoneyException() {
    }

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}

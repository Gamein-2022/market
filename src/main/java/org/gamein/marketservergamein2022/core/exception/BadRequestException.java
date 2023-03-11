package org.gamein.marketservergamein2022.core.exception;

import lombok.Getter;


@Getter
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {}
}

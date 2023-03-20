package org.gamein.marketservergamein2022.core.dto.result;

import lombok.Getter;


@Getter
public class ServiceResult<T> extends BaseResult {
    private final T result;

    private ServiceResult(T result) {
        this.result = result;
    }
    public static <T> ServiceResult<T> createResult(T result) {
        return new ServiceResult<>(result);
    }
}
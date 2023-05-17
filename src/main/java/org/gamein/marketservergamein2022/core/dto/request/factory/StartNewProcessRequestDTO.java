package org.gamein.marketservergamein2022.core.dto.request.factory;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
public class StartNewProcessRequestDTO {
    private int count;
    private long lineId;
    private long productId;
}

package org.gamein.marketservergamein2022.core.dto.request.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public class RemoveFromStorageRequestDTO {
    private long productId;
    private int quantity;
}

package org.gamein.marketservergamein2022.core.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LogType;

@Getter
@Setter
@AllArgsConstructor
public class LogDTO {
    private LogType type;
    private Long totalCost;
    private Long count;
    private String productName;

}

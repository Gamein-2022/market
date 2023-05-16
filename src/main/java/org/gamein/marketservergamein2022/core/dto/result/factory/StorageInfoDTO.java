package org.gamein.marketservergamein2022.core.dto.result.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
@Getter
public class StorageInfoDTO {
    List<StorageProductDTO> products;
    private double inStoragePercent;
    private double manufacturingPercent;
    private double emptyPercent;
    private double blockedPercent;
    private int storageSpace;
    private boolean isStorageUpgraded;
    private long storageUpgradeCost;
}

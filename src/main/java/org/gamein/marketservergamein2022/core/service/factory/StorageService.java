package org.gamein.marketservergamein2022.core.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.StorageInfoDTO;
import org.gamein.marketservergamein2022.core.dto.result.market.ShippingDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;

import java.util.List;


public interface StorageService {
    StorageInfoDTO getStorageInfo(Team team);

    List<ShippingDTO> getStorageQueue(Team team);

    StorageInfoDTO collectFromQueue(Team team, Long shippingId)
            throws NotFoundException, BadRequestException;

    StorageInfoDTO removeFromQueue(Team team, Long shippingId)
            throws NotFoundException, BadRequestException;

    List<ShippingDTO> getInRouteShippings(Team team);

    StorageInfoDTO removeFromStorage(Team team, Long productId, Integer quantity)
            throws BadRequestException;

    void upgradeStorage(Team team) throws BadRequestException;
}

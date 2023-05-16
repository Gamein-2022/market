package org.gamein.marketservergamein2022.core.dto.request.factory;

import lombok.Getter;

import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;


@Getter
public class CreateBuildingRequestDTO {
    private BuildingType type;
    private Byte ground;
}
package org.gamein.marketservergamein2022.core.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.BuildingDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.GroundDetailsDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.TeamBuildingsResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotEnoughMoneyException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;


public interface BuildingService {
    TeamBuildingsResult getTeamBuildings(Team team);

    BuildingDTO createBuilding(Long teamId, BuildingType type, Byte ground) throws BadRequestException,
            NotEnoughMoneyException;

    BuildingDTO upgradeBuilding(Long teamId, long buildingId) throws BadRequestException, NotFoundException;

    TeamBuildingsResult destroyBuilding(Long teamId, Byte ground) throws NotFoundException, BadRequestException;

    void upgradeRegion(Team team)
            throws NotEnoughMoneyException, BadRequestException;

    GroundDetailsDTO getGroundDetails(Team team, Byte ground);
}
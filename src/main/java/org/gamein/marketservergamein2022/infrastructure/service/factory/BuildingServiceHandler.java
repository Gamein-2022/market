package org.gamein.marketservergamein2022.infrastructure.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.BuildingDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.GroundDetailsDTO;
import org.gamein.marketservergamein2022.core.dto.result.factory.TeamBuildingsResult;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotEnoughMoneyException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.factory.BuildingService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineStatus;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.LineType;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.ProductGroup;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamDateRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingInfoRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.FactoryLineRepository;
import org.gamein.marketservergamein2022.infrastructure.util.RestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class BuildingServiceHandler implements BuildingService {
    private final BuildingRepository buildingRepository;
    private final TeamRepository teamRepository;
    private final FactoryLineRepository factoryLineRepository;
    private final TimeRepository timeRepository;
    private final BuildingInfoRepository buildingInfoRepository;

    private final TeamDateRepository teamDateRepository;

    public BuildingServiceHandler(BuildingRepository buildingRepository, TeamRepository teamRepository, FactoryLineRepository factoryLineRepository, TimeRepository timeRepository, BuildingInfoRepository buildingInfoRepository, TeamDateRepository teamDateRepository) {
        this.buildingRepository = buildingRepository;
        this.teamRepository = teamRepository;
        this.factoryLineRepository = factoryLineRepository;
        this.timeRepository = timeRepository;
        this.buildingInfoRepository = buildingInfoRepository;
        this.teamDateRepository = teamDateRepository;
    }

    @Value("${live.data.url}")
    private String liveUrl;

    @Override
    public TeamBuildingsResult getTeamBuildings(Team team) {
        ArrayList<Byte> grounds = new ArrayList<>() {{
            add((byte) 0);
            add((byte) 1);
            add((byte) 2);
            add((byte) 3);
        }};
        return new TeamBuildingsResult(
                grounds.stream().map(ground -> {
                    Optional<Building> buildingOptional = buildingRepository.findByGroundAndTeam_Id(ground, team.getId());
                    return buildingOptional.map(Building::toDTO).orElse(null);
                }).toList(),
                team.getIsRegionUpgraded(),
                timeRepository.findById(1L).get().getUpgradeRegionPrice(),
                team.getIsStorageUpgraded()
        );
    }

    @Override
    public BuildingDTO createBuilding(Team team, BuildingType type, Byte ground)
            throws BadRequestException, NotEnoughMoneyException {
        teamDateRepository.updateTeamDate(LocalDateTime.now(ZoneOffset.UTC),team.getId());
        BuildingInfo info = buildingInfoRepository.findById(type).orElseGet(BuildingInfo::new);
        validateCreatingBuilding(team, type, ground, info);

        Building building = new Building();
        building.setType(type);
        if (type == BuildingType.RECYCLE_FACTORY) {
            building.setGround((byte) 0);
        } else {
            building.setGround(ground);
        }
        building.setUpgraded(false);
        building.setTeam(team);
        building.setLines(new ArrayList<>());


        long balance = team.getBalance();
        balance -= info.getBuildPrice();
        team.setBalance(balance);
        List<Building> buildings = team.getBuildings();
        buildings.add(building);
        team.setBuildings(buildings);


        LineType lineType;
        switch (building.getType()) {
            case PRODUCTION_FACTORY -> lineType = LineType.PRODUCTION;
            case ASSEMBLY_FACTORY -> lineType = LineType.ASSEMBLY;
            default -> lineType = LineType.RECYCLE;
        }
        List<FactoryLine> factoryLines = new ArrayList<>();

        buildingRepository.save(building);
        for (int i = 0; i < info.getBaseLineCount(); i++) {
            factoryLines.add(createLine(team.getId(), building, lineType));
        }
        factoryLineRepository.saveAll(factoryLines);
        buildingRepository.save(building);
        teamRepository.save(team);

        RestUtil.sendNotificationToATeam("", "UPDATE_MAP", String.valueOf(team.getId()), liveUrl);

        return building.toDTO();
    }

    @Override
    public BuildingDTO upgradeBuilding(Team team, long buildingId) throws BadRequestException, NotFoundException {
        teamDateRepository.updateTeamDate(LocalDateTime.now(ZoneOffset.UTC),team.getId());
        Optional<Building> buildingOptional = team.getBuildings().stream().filter(b -> b.getId() == buildingId).findFirst();
        if (buildingOptional.isEmpty()) throw new NotFoundException("ساختمان یافت نشد!");

        Building building = buildingOptional.get();
        if (!building.getTeam().getId().equals(team.getId())) throw new NotFoundException("ساختمان یافت نشد!");

        BuildingInfo info = buildingInfoRepository.findById(building.getType()).orElseGet(BuildingInfo::new);

        if (team.getBalance() < info.getUpgradePrice())
            throw new BadRequestException("پول نداری :)");

        if (building.isUpgraded()) throw new BadRequestException("ساختمان قبلا ارتقا یافته است!");

        building.setUpgraded(true);


        LineType lineType;
        switch (building.getType()) {
            case PRODUCTION_FACTORY -> lineType = LineType.PRODUCTION;
            case ASSEMBLY_FACTORY -> lineType = LineType.ASSEMBLY;
            default -> lineType = LineType.RECYCLE;
        }
        List<FactoryLine> factoryLines = new ArrayList<>();

        for (int i = info.getBaseLineCount(); i < info.getUpgradeLineCount(); i++) {
            factoryLines.add(createLine(team.getId(), building, lineType));
        }
        team.setBalance(team.getBalance() - info.getUpgradePrice());
        factoryLineRepository.saveAll(factoryLines);
        buildingRepository.save(building);
        teamRepository.save(team);

        RestUtil.sendNotificationToATeam("", "UPDATE_MAP", String.valueOf(team.getId()), liveUrl);

        return building.toDTO();
    }

    @Override
    public TeamBuildingsResult destroyBuilding(Team team, Byte ground) throws NotFoundException,
            BadRequestException {
        teamDateRepository.updateTeamDate(LocalDateTime.now(ZoneOffset.UTC),team.getId());
        Optional<Building> buildingOptional = buildingRepository.findByGroundAndTeam_Id(ground, team.getId());
        if (buildingOptional.isEmpty()) throw new NotFoundException("ساختمان مورد نظر یافت نشد!");

        Building building = buildingOptional.get();
        BuildingInfo info = buildingInfoRepository.findById(building.getType()).orElseGet(BuildingInfo::new);

        int lineCount = building.isUpgraded() ? info.getUpgradeLineCount() : info.getBaseLineCount();
        List<FactoryLine> lines = factoryLineRepository.findALlByBuilding_IdAndEndTimeIsNull(building.getId());
        if (lines.size() < lineCount) throw new BadRequestException("خطوط تولید مشغول کارند!");


        team.getBuildings().removeIf(b -> building.getId() == b.getId());
        team.setBalance(team.getBalance() +
                (int) (0.9 * info.getBuildPrice()) +
                (building.isUpgraded() ? (int) (0.9 * info.getUpgradePrice()) : 0)
        );
        buildingRepository.delete(building);
        factoryLineRepository.deleteAll(lines);
        teamRepository.save(team);

        RestUtil.sendNotificationToATeam("", "UPDATE_MAP", String.valueOf(team.getId()), liveUrl);

        return getTeamBuildings(team);
    }

    @Override
    public void upgradeRegion(Team team)
            throws NotEnoughMoneyException, BadRequestException {
        Time time = timeRepository.findById(1L).get();
        if (team.getIsRegionUpgraded())
            throw new BadRequestException("زمین شما قبلا ارتقا یافته است.");
        if (team.getBalance() < time.getUpgradeRegionPrice()) {
            throw new NotEnoughMoneyException("بودجه شما برای ارتقای زمین کافی نیست!");
        }
        team.setBalance(team.getBalance() - time.getUpgradeRegionPrice());
        team.setIsRegionUpgraded(true);

        RestUtil.sendNotificationToATeam("", "UPDATE_MAP", String.valueOf(team.getId()), liveUrl);

        teamRepository.save(team);
    }

    @Override
    public GroundDetailsDTO getGroundDetails(Team team, Byte ground) {
        Optional<Building> buildingOptional = buildingRepository.findByGroundAndTeam_Id(ground, team.getId());

        return new GroundDetailsDTO(
                buildingInfoRepository.findById(BuildingType.PRODUCTION_FACTORY).orElseGet(BuildingInfo::new).getBuildPrice(),
                buildingInfoRepository.findById(BuildingType.ASSEMBLY_FACTORY).orElseGet(BuildingInfo::new).getBuildPrice(),
                buildingInfoRepository.findById(BuildingType.RECYCLE_FACTORY).orElseGet(BuildingInfo::new).getBuildPrice(),
                timeRepository.findById(1L).get().getUpgradeRegionPrice(),
                ground != 3 || team.getIsRegionUpgraded(),
                buildingOptional.map(building -> building.toDetailsDTO(
                        buildingInfoRepository.findById(building.getType()).orElseGet(BuildingInfo::new).getUpgradePrice()
                )).orElse(null)
        );
    }

    private void validateCreatingBuilding(Team team, BuildingType type, Byte ground, BuildingInfo info) throws NotEnoughMoneyException,
            BadRequestException {
        if (ground == 0 && type != BuildingType.RECYCLE_FACTORY)
            throw new BadRequestException("این زمین مختص سوله‌ی بازیافت است!");
        if (ground < 0 || ground > 3)
            throw new BadRequestException("چنین زمینی برای ساخت سوله وجود ندارد!");
        if (ground == 3 && !team.getIsRegionUpgraded())
            throw new BadRequestException("شما هنوز این زمین را در اختیار ندارید!");

        if (team.getBalance() < info.getBuildPrice()) {
            throw new NotEnoughMoneyException("بودجه شما برای خرید این ساختمان کافی نیست!");
        }

        boolean isRecycleBeanCreated = false;
        int manufacturingBuildingsNumber = 0;
        for (Building building : team.getBuildings()) {
            if (ground.equals(building.getGround()))
                throw new BadRequestException("شما در این زمین ساختمان دارید!");

            if (building.getType().equals(BuildingType.PRODUCTION_FACTORY) || building.getType().equals(BuildingType.ASSEMBLY_FACTORY))
                manufacturingBuildingsNumber += 1;
            else if (building.getType().equals(BuildingType.RECYCLE_FACTORY))
                isRecycleBeanCreated = true;
        }

        if (type.equals(BuildingType.PRODUCTION_FACTORY) || type.equals(BuildingType.ASSEMBLY_FACTORY)) {
            if (manufacturingBuildingsNumber == 2 && !team.getIsRegionUpgraded())
                throw new BadRequestException("برای ساخت یک سوله دیگر ابتدا زمین خود را ارتقا دهید.");
            if (manufacturingBuildingsNumber == 3)
                throw new BadRequestException("تعدد سوله های های شما در بیشترین تعداد ممکن است!");
        }

        if (type.equals(BuildingType.RECYCLE_FACTORY) && isRecycleBeanCreated)
            throw new BadRequestException("سوله‌ی بازیافت قبلا ساخته شده است!");
    }

    private FactoryLine createLine(Long teamId, Building building, LineType lineType) {
        FactoryLine factoryLine = new FactoryLine();
        factoryLine.setStatus(LineStatus.NOT_INITIAL);
        factoryLine.setTeamId(teamId);
        factoryLine.setType(lineType);
        if (lineType == LineType.RECYCLE) {
            factoryLine.setStatus(LineStatus.OFF);
            factoryLine.setInitiationDate(LocalDateTime.now(ZoneOffset.UTC));
            factoryLine.setGroup(ProductGroup.RECYCLE);
        }
        building.addLine(factoryLine);
        return factoryLine;
    }
}

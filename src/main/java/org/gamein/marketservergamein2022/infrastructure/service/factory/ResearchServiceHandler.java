package org.gamein.marketservergamein2022.infrastructure.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.TeamResearchDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.factory.ResearchService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.BuildingType;
import org.gamein.marketservergamein2022.infrastructure.repository.StorageProductRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingInfoRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.BuildingRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.ResearchSubjectRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.TeamResearchRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.gamein.marketservergamein2022.infrastructure.util.TeamUtil.getTeamWealth;


@Service
public class ResearchServiceHandler implements ResearchService {
    private final TeamResearchRepository teamResearchRepository;
    private final ResearchSubjectRepository researchSubjectRepository;
    private final TeamRepository teamRepository;
    private final TimeRepository timeRepository;
    private final StorageProductRepository storageProductRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingInfoRepository buildingInfoRepository;

    public ResearchServiceHandler(TeamResearchRepository teamResearchRepository,
                                  ResearchSubjectRepository researchSubjectRepository,
                                  TeamRepository teamRepository, TimeRepository timeRepository, StorageProductRepository storageProductRepository, BuildingRepository buildingRepository, BuildingInfoRepository buildingInfoRepository) {
        this.teamResearchRepository = teamResearchRepository;
        this.researchSubjectRepository = researchSubjectRepository;
        this.teamRepository = teamRepository;
        this.timeRepository = timeRepository;
        this.storageProductRepository = storageProductRepository;
        this.buildingRepository = buildingRepository;
        this.buildingInfoRepository = buildingInfoRepository;
    }

    @Override
    public List<TeamResearchDTO> getTeamResearches(Team team) {
        return teamResearchRepository.findAllByTeam_Id(team.getId()).stream()
                .map(r -> r.toDTO(team.getBalance(), 0, 0)).collect(Collectors.toList());
    }

    @Override
    public TeamResearchDTO startResearchProcess(Team team, String name)
            throws BadRequestException, NotFoundException {
        if (teamResearchRepository.findFirstByEndTimeAfterAndTeamId(LocalDateTime.now(ZoneOffset.UTC), team.getId()) != null) {
            throw new BadRequestException("شما یک فرآیند تحقیق و توسعه در دست انجام دارید!");
        }
        ResearchSubject subject = researchSubjectRepository.findByName(name);
        if (subject == null) {
            throw new NotFoundException("تحقیق مورد نظر یافت نشد!");
        }
        if (getEligibleTeams(subject, subject.getParent() == null ? teamRepository.findAll().stream() :
                teamResearchRepository.findAllBySubject_IdAndEndTimeBefore(subject.getId(),
                        LocalDateTime.now(ZoneOffset.UTC)).stream().map(TeamResearch::getTeam)).noneMatch(t -> t.getId().equals(team.getId()))) {
            throw new BadRequestException("شما امکان سرمایه‌گذاری در این تحقیق و توسعه را ندارید!");
        }
        Optional<TeamResearch> teamResearchOptional =
                teamResearchRepository.findByTeam_IdAndSubject_Name(team.getId(), name);
        if (teamResearchOptional.isPresent()) {
            throw new BadRequestException("شما این فرآیند را قبلا انجام داده‌اید!");
        }

        Time time = timeRepository.findById(1L).get();
        LocalDateTime beginDate = time.getBeginTime();
        Long stoppedSeconds = time.getStoppedTimeSeconds();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        long durationSeconds = Duration.between(beginDate, now).toSeconds() - stoppedSeconds - time.getChooseRegionDuration();

        long daySeconds = 8L;
        long daysFromBeginning = durationSeconds / daySeconds;

        if (subject.getAvailableDay() > daysFromBeginning) {
            throw new BadRequestException("هنوز زمان تحقیق مورد نظر نرسیده است!");
        }

        double N_tOnN =
                (double) teamResearchRepository.getResearchCount(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)) / teamRepository.getTeamsCount();

        int duration = calculateDuration(subject, N_tOnN);

        int price = calculatePrice(subject);

        long balance = team.getBalance();
        if (balance < price) {
            throw new BadRequestException("اعتبار شما کافی نیست!");
        }
        balance -= price;
        team.setBalance(balance);
        teamRepository.save(team);

        TeamResearch research = new TeamResearch();
        research.setTeam(team);
        research.setSubject(subject);
        research.setPaidAmount(price);
        research.setBeginTime(LocalDateTime.now(ZoneOffset.UTC));
        research.setEndTime(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(duration));
        teamResearchRepository.save(research);

        return research.toDTO(team.getBalance(), price, duration * 1000);
    }

    @Override
    public TeamResearchDTO getCurrentResearch(Team team) {
        try {
            return teamResearchRepository.findFirstByEndTimeAfterAndTeamId(LocalDateTime.now(ZoneOffset.UTC), team.getId()).toDTO(team.getBalance(), 0, 0);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public TeamResearchDTO getSubjectInfo(Team team, String name)
            throws NotFoundException {
        Optional<TeamResearch> researchOptional = teamResearchRepository.findByTeam_IdAndSubject_Name(
                team.getId(),
                name
        );
        if (researchOptional.isPresent()) {
            return researchOptional.get().toDTO(team.getBalance(), 0, 0);
        }
        ResearchSubject subject = researchSubjectRepository.findByName(name);
        if (subject == null) {
            throw new NotFoundException("تحقیق و توسعه درخواست شده وجود ندارد!");
        }
        if (getEligibleTeams(subject, subject.getParent() == null ?
                teamRepository.findAll().stream().filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())) :
                teamResearchRepository.findAllBySubject_IdAndEndTimeBefore(subject.getParent().getId(),
                        LocalDateTime.now(ZoneOffset.UTC)).stream().map(TeamResearch::getTeam)
                        .filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())))
                .noneMatch(t -> t.getId().equals(team.getId()))) {
            TeamResearch teamResearch = new TeamResearch();
            teamResearch.setSubject(subject);
            return teamResearch.toDTO(team.getBalance(), -1, -1);
        }

        double N_tOnN =
                (double) teamResearchRepository.getResearchCount(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)) / teamRepository.getTeamsCount();

        int duration = calculateDuration(subject, N_tOnN);

        int price = calculatePrice(subject);

        TeamResearch teamResearch = new TeamResearch();
        teamResearch.setSubject(subject);
        return teamResearch.toDTO(team.getBalance(), price, duration * 1000);
    }

    @Override
    public TeamResearchDTO stopResearch(Team team, String name)
            throws BadRequestException {
        Optional<TeamResearch> researchOptional = teamResearchRepository.findByTeam_IdAndSubject_Name(
                team.getId(),
                name
        );
        if (researchOptional.isEmpty()) {
            throw new BadRequestException("شما فرآیندی با این نام در دست انجام ندارید!");
        }
        TeamResearch research = researchOptional.get();
        if ((double) Duration.between(LocalDateTime.now(ZoneOffset.UTC), research.getBeginTime()).toMillis()
                / Duration.between(research.getEndTime(), research.getBeginTime()).toMillis() >= 0.5) {
            throw new BadRequestException("شما دیگر امکان انصراف ندارید!");
        }
        teamResearchRepository.delete(research);
        team.setBalance(team.getBalance() + (int) (0.9 * research.getPaidAmount()));
        teamRepository.save(team);
        TeamResearch teamResearch = new TeamResearch();

        ResearchSubject subject = research.getSubject();
        teamResearch.setSubject(subject);

        double N_tOnN =
                (double) teamResearchRepository.getResearchCount(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)) / teamRepository.getTeamsCount();

        int duration = calculateDuration(subject, N_tOnN);

        int price = calculatePrice(subject);

        return teamResearch.toDTO(team.getBalance(), price, duration * 1000);
    }

    private Stream<Team> getEligibleTeams(ResearchSubject subject, Stream<Team> teams) {
        if (subject.getProductGroup() != null) {
            return teams.filter(team -> {
                for (Building building : team.getBuildings()) {
                    for (FactoryLine line : building.getLines()) {
                        if (line.getGroup() == subject.getProductGroup()) {
                            return true;
                        }
                    }
                }
                return false;
            });
        } else {
            return teams.filter(team -> {
                for (Building building : team.getBuildings()) {
                    if (building.getType() == subject.getBuildingType()) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private int calculatePrice(ResearchSubject subject) {
        double medianTeamBalance;
        Stream<Team> teamsStream;
        if (subject.getParent() == null) {
            List<Team> teams = teamRepository.findAll();
            teamsStream = getEligibleTeams(subject, teams.stream()
                    .filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())));
        } else {
            teamsStream =
                    getEligibleTeams(subject, teamResearchRepository.findAllBySubject_IdAndEndTimeBefore(subject.getParent().getId(),
                            LocalDateTime.now(ZoneOffset.UTC)).stream().map(TeamResearch::getTeam)
                            .filter(t -> !teamResearchRepository.existsByTeam_IdAndSubject_Id(t.getId(), subject.getId())));
        }
        List<Double> teamsBalances =
                teamsStream.map(
                        team -> (double) getTeamWealth(team, storageProductRepository, buildingRepository,
                                buildingInfoRepository)
                                - calculateBuildingsCost(team.getBuildings())
                ).sorted().toList();
        if (teamsBalances.size() == 0) {
            return -1;
        }
        medianTeamBalance = teamsBalances.size() % 2 == 0 ?
                (teamsBalances.get(teamsBalances.size() / 2 - 1) + teamsBalances.get(teamsBalances.size() / 2)) / 2 :
                teamsBalances.get(teamsBalances.size() / 2);
        double alpha = timeRepository.findById(1L).get().getRAndDPriceMultiplier();

        return (int) (alpha * medianTeamBalance);
    }

    private int calculateDuration(ResearchSubject subject, double N_tOnN) {
        Time time = timeRepository.findById(1L).get();
        if (subject.getDurationBound() != null) {
            return (int) (time.getRAndDTimeCoeff() * (calculateDuration(subject.getDurationBound(), N_tOnN)));
        }
        double baseTime = subject.getBaseDuration();
        baseTime -= 60 * Math.sqrt(N_tOnN);
        TeamResearch firstFinishedResearch;
        try {
            firstFinishedResearch = teamResearchRepository.findFirstResearch(subject.getId(), LocalDateTime.now(ZoneOffset.UTC)).get(0);
        } catch (IndexOutOfBoundsException e) {
            firstFinishedResearch = null;
        }
        if (firstFinishedResearch != null) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime half = firstFinishedResearch.getBeginTime().plus(
                    Duration.between(firstFinishedResearch.getBeginTime(),
                            firstFinishedResearch.getEndTime()).toMillis() / 2, ChronoUnit.MILLIS
            );
            double diff = Math.abs(Duration.between(now, half).toMinutes());
            baseTime -= 2 * Math.sqrt(
                    diff / time.getRAndDRush()
            );
        }
        baseTime = Math.max(baseTime, 10);
        return ((int) (baseTime * 60));
    }

    private int calculateBuildingsCost(List<Building> buildings) {
        int result = 0;
        for (BuildingType type : BuildingType.values()) {
            result += buildings.stream().filter(building -> building.getType() == type).count() *
                    buildingInfoRepository.findById(type).orElseGet(BuildingInfo::new).getBuildPrice();
        }
        return result;
    }
}

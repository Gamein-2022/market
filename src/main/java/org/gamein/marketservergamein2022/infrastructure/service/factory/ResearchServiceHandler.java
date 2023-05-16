package org.gamein.marketservergamein2022.infrastructure.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.TeamResearchDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.service.factory.ResearchService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.*;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.TimeRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.ResearchSubjectRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.factory.TeamResearchRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class ResearchServiceHandler implements ResearchService {
    private final TeamResearchRepository teamResearchRepository;
    private final ResearchSubjectRepository researchSubjectRepository;
    private final TeamRepository teamRepository;
    private final TimeRepository timeRepository;

    public ResearchServiceHandler(TeamResearchRepository teamResearchRepository,
                                  ResearchSubjectRepository researchSubjectRepository,
                                  TeamRepository teamRepository, TimeRepository timeRepository) {
        this.teamResearchRepository = teamResearchRepository;
        this.researchSubjectRepository = researchSubjectRepository;
        this.teamRepository = teamRepository;
        this.timeRepository = timeRepository;
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
        Optional<TeamResearch> teamResearchOptional =
                teamResearchRepository.findByTeam_IdAndSubject_Name(team.getId(), name);
        if (teamResearchOptional.isPresent()) {
            throw new BadRequestException("شما این فرآیند را قبلا انجام داده‌اید!");
        }
        if (getEligibleTeams(subject, subject.getParent() == null ? teamRepository.findAll().stream() :
                teamResearchRepository.findAllBySubject_IdAndEndTimeBefore(subject.getId(),
                        LocalDateTime.now(ZoneOffset.UTC)).stream().map(TeamResearch::getTeam)).noneMatch(t -> t.getId().equals(team.getId()))) {
            throw new BadRequestException("شما امکان سرمایه‌گذاری در این تحقیق و توسعه را ندارید!");
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

        int duration = calculateDuration(subject);

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

        int duration = calculateDuration(subject);

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

        int duration = calculateDuration(subject);

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
        return subject.getPrice();
    }

    private int calculateDuration(ResearchSubject subject) {
        return subject.getDuration();
    }
}

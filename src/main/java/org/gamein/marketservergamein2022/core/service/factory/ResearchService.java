package org.gamein.marketservergamein2022.core.service.factory;


import org.gamein.marketservergamein2022.core.dto.result.factory.TeamResearchDTO;
import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.exception.NotFoundException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;

import java.util.List;

public interface ResearchService {
    List<TeamResearchDTO> getTeamResearches(Team team);
    TeamResearchDTO startResearchProcess(Team team, String name)
            throws BadRequestException, NotFoundException;
    TeamResearchDTO getCurrentResearch(Team team);
    TeamResearchDTO getSubjectInfo(Team team, String name)
            throws NotFoundException;

    TeamResearchDTO stopResearch(Team team, String name)
            throws BadRequestException;
}

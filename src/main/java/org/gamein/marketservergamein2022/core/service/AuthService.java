package org.gamein.marketservergamein2022.core.service;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.User;

public interface AuthService {
    Team getTeamById(Long teamId) throws BadRequestException;
    User getUserById(Long userId) throws BadRequestException;
}

package org.gamein.marketservergamein2022.infrastructure.service.dashboard;

import org.gamein.marketservergamein2022.core.exception.BadRequestException;
import org.gamein.marketservergamein2022.core.service.dashboard.AuthService;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.User;
import org.gamein.marketservergamein2022.infrastructure.repository.TeamRepository;
import org.gamein.marketservergamein2022.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceHandler implements AuthService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public AuthServiceHandler(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Team getTeamById(Long teamId) throws BadRequestException {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (teamOptional.isEmpty()) {
            throw new BadRequestException("Team does not exist!");
        }
        return teamOptional.get();
    }

    @Override
    public User getUserById(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        return userOptional.get();
    }
}

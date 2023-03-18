package org.gamein.marketservergamein2022.core.iao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.User;


@AllArgsConstructor
@Getter
public class AuthInfo {
    private User user;
    private Team team;
}

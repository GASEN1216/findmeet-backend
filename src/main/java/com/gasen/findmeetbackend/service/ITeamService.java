package com.gasen.findmeetbackend.service;

import com.gasen.findmeetbackend.model.Request.AddTeamRequest;
import com.gasen.findmeetbackend.model.Request.JoinTeamRequest;
import com.gasen.findmeetbackend.model.Request.TeamRequest;
import com.gasen.findmeetbackend.model.Request.UpdateTeamRequest;
import com.gasen.findmeetbackend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.findmeetbackend.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 队伍表 服务类
 * </p>
 *
 * @author gasen
 * @since 2024-03-13
 */
public interface ITeamService extends IService<Team> {

    Team addTeam(AddTeamRequest team, HttpServletRequest request);

    int updateTeam(UpdateTeamRequest team, HttpServletRequest request);

    Boolean deleteTeam(int teamId, HttpServletRequest request);

    Boolean quitTeam(int teamId, HttpServletRequest request);

    Boolean joinTeam(JoinTeamRequest team, HttpServletRequest request);

    List<Team> queryUserJoinTeam(HttpServletRequest request);

    List<User> queryUserJoinTeamMembers(int teamId, HttpServletRequest request);
}

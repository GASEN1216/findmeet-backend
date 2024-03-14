package com.gasen.findmeetbackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.findmeetbackend.common.BaseResponse;
import com.gasen.findmeetbackend.common.ErrorCode;
import com.gasen.findmeetbackend.common.PageRequest;
import com.gasen.findmeetbackend.common.ResultUtils;
import com.gasen.findmeetbackend.exception.BusinessExcetion;
import com.gasen.findmeetbackend.mapper.TeamMapper;
import com.gasen.findmeetbackend.model.Enum.TeamEnum;
import com.gasen.findmeetbackend.model.Request.AddTeamRequest;
import com.gasen.findmeetbackend.model.Request.JoinTeamRequest;
import com.gasen.findmeetbackend.model.Request.TeamRequest;
import com.gasen.findmeetbackend.model.Request.UpdateTeamRequest;
import com.gasen.findmeetbackend.model.domain.Team;
import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.service.impl.TeamServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gasen.findmeetbackend.constant.UserConstant.USER_LOGIN_IN;

/**
 * <p>
 * 队伍表 前端控制器
 * </p>
 *
 * @author gasen
 * @since 2024-03-13
 */
@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TeamServiceImpl teamService;

    @Operation(summary = "创建队伍")
    @PostMapping("/add")
    public BaseResponse<Team> addTeam(@RequestBody AddTeamRequest team, HttpServletRequest request) {
        isLoginIn(request);
        if(team == null) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.addTeam(team, request));
    }

    @Operation(summary = "更新队伍信息")
    @PostMapping("/update")
    public BaseResponse<Integer> updateTeam(@RequestBody UpdateTeamRequest team, HttpServletRequest request) {
        isLoginIn(request);
        if(team == null) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.updateTeam(team, request));
    }

    @Operation(summary = "解散删除队伍")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(int teamId, HttpServletRequest request) {
        isLoginIn(request);
        if(teamId < 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.deleteTeam(teamId, request));
    }

    @Operation(summary = "退出队伍")
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(int teamId, HttpServletRequest request) {
        isLoginIn(request);
        if(teamId < 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.quitTeam(teamId, request));
    }

    @Operation(summary = "加入队伍")
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest team, HttpServletRequest request) {
        isLoginIn(request);
        if(team == null) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.joinTeam(team, request));
    }

    @Operation(summary = "根据名称查询队伍")
    @GetMapping("/query/team")
    public BaseResponse<Team> queryTeamByName(@RequestParam("name") String teamName, HttpServletRequest request) {
        isLoginIn(request);
        if(StringUtils.isBlank(teamName)) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        Team team = teamService.lambdaQuery().eq(Team::getName, teamName).one();
        if(team == null) throw new BusinessExcetion(ErrorCode.TEAM_NOT_EXIST);
        return ResultUtils.success(team);
    }

    @Operation(summary = "查询用户加入的队伍")
    @GetMapping("/query/myTeams")
    public BaseResponse<List<Team>> queryUserJoinTeam(HttpServletRequest request) {
        isLoginIn(request);
        return ResultUtils.success(teamService.queryUserJoinTeam(request));
    }

    @Operation(summary = "根据队伍id查询成员")
    @GetMapping("/query/members")
    public BaseResponse<List<User>> queryUserJoinTeamMembers(@RequestParam("teamId") int teamId, HttpServletRequest request) {
        isLoginIn(request);
        if(teamId < 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(teamService.queryUserJoinTeamMembers(teamId, request));
    }

    @Operation(summary = "查询所有队伍")
    @GetMapping("/query/all")
    public BaseResponse<Page<Team>> queryAllTeam(long pageNum, long pageSize, HttpServletRequest request) {
        isLoginIn(request);
        String redisKey = String.format("gasen:findmeetbackend:team:query:all:%s:%s", pageNum, pageSize);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<Team> teamPage = (Page<Team>) valueOperations.get(redisKey);
        if(teamPage !=null) {
            return ResultUtils.success(teamPage);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("state", TeamEnum.PRIVATE.getState());
        Page<Team> teamList = teamService.page(new Page<>(pageNum, pageSize), queryWrapper);
        valueOperations.set(redisKey, teamList, 10, TimeUnit.SECONDS);
        log.info("/query/all 缓存预热成功");
        return ResultUtils.success(teamList);
    }

    public static void isLoginIn(HttpServletRequest request) {
        if(request.getSession().getAttribute(USER_LOGIN_IN) == null) throw new BusinessExcetion(ErrorCode.USER_NOT_LOGIN);
    }
}

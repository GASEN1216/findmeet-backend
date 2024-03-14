package com.gasen.findmeetbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gasen.findmeetbackend.common.ErrorCode;
import com.gasen.findmeetbackend.controller.UserController;
import com.gasen.findmeetbackend.exception.BusinessExcetion;
import com.gasen.findmeetbackend.mapper.UserMapper;
import com.gasen.findmeetbackend.mapper.UserTeamMapper;
import com.gasen.findmeetbackend.model.Enum.TeamEnum;
import com.gasen.findmeetbackend.model.Request.AddTeamRequest;
import com.gasen.findmeetbackend.model.Request.JoinTeamRequest;
import com.gasen.findmeetbackend.model.Request.TeamRequest;
import com.gasen.findmeetbackend.model.Request.UpdateTeamRequest;
import com.gasen.findmeetbackend.model.domain.Team;
import com.gasen.findmeetbackend.mapper.TeamMapper;
import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.model.domain.UserTeam;
import com.gasen.findmeetbackend.service.ITeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.findmeetbackend.service.IUserService;
import com.gasen.findmeetbackend.utils.AlgorithmUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.gasen.findmeetbackend.constant.UserConstant.USER_LOGIN_IN;

/**
 * <p>
 * 队伍表 服务实现类
 * </p>
 *
 * @author gasen
 * @since 2024-03-13
 */
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    @Resource
    private IUserService userService;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Resource
    RedissonClient redissonClient;

    /**
     * 添加队伍
     * @param addTeamRequest
     * @return Team
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Team addTeam(AddTeamRequest addTeamRequest, HttpServletRequest request) {
        // 获取用户,将用户id传入进去
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        TeamRequest team = new TeamRequest();
        BeanUtils.copyProperties(addTeamRequest, team);
        team.setCreateUser(user.getId());
        // 检查队伍信息是否合法
        inspectTeam(team, true);
        RLock rLock = redissonClient.getLock("gasen:findmeetbackend:team:add");
        try {
            while(true) {
                if(rLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    //防止重复添加
                    inspectTeam(team, true);
                    System.out.println("创建队伍："+Thread.currentThread().getName()+"getLock");
                    Team team1 = new Team();
                    BeanUtils.copyProperties(team, team1);
                    team1.setId(null);
                    team1.setMaxNum(1);
                    if(this.save(team1)){
                        UserTeam userTeam = new UserTeam();
                        userTeam.setUserId(user.getId()).setTeamId(team1.getId());
                        userTeamMapper.insert(userTeam);
                        return this.getById(team1.getId());
                    }
                    else
                        throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "添加队伍失败");
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(rLock.isHeldByCurrentThread()) {
                System.out.println("创建队伍："+Thread.currentThread().getName()+"freeLock");
                rLock.unlock();
            }
        }
        return null;
    }

    /**
     * 更新队伍信息
     * @param updateTeamRequest
     * @return Integer
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateTeam(UpdateTeamRequest updateTeamRequest, HttpServletRequest request) {
        TeamRequest team = new TeamRequest();
        BeanUtils.copyProperties(updateTeamRequest, team);
        inspectTeam(team, false);
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        //队伍创建者才能修改
        if(lambdaQuery().eq(Team::getId,team.getId()).eq(Team::getCreateUser,user.getId()).exists()) {
            Team team1 = new Team();
            BeanUtils.copyProperties(team, team1);
            team1.setMaxNum(baseMapper.selectById(team.getId()).getMaxNum());
            if(!user.getId().equals(team1.getId())) {
                UserTeam source = new UserTeamServiceImpl().lambdaQuery().eq(UserTeam::getUserId, user.getId()).one();
                source.setUserId(team1.getId());
                userTeamMapper.updateById(source);
            }
            return baseMapper.updateById(team1);
        }
        throw new BusinessExcetion(ErrorCode.POWERLESS);
    }

    /**
     * 删除队伍
     * @param teamId
     * @param request
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(int teamId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        if(lambdaQuery().eq(Team::getId,teamId).eq(Team::getCreateUser,user.getId()).exists()) {
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("team_id",teamId);
            List<Integer> members = userTeamMapper.selectList(userTeamQueryWrapper).stream().map(UserTeam::getUserId).toList();
            return baseMapper.deleteById(teamId) > 0 && userTeamMapper.deleteBatchIds(members) > 0;
        } else throw new BusinessExcetion(ErrorCode.POWERLESS);
    }


    /**
     * 退出队伍
     * @param teamId
     * @param request
     * @return Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean quitTeam(int teamId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.select("id").eq("team_id",teamId).eq("user_id",user.getId());
        if(userTeamMapper.selectOne(userTeamQueryWrapper)!=null) {
            Team team = new Team();
            team.setId(teamId);
            team.setMaxNum(baseMapper.selectById(teamId).getMaxNum()-1);
            //TODO： 如果退出的是队伍的创建者,则自动转让给第二个加入队伍的人
            //如果退出的是队伍的创建者，则不允许
            if(baseMapper.selectById(teamId).getCreateUser().equals(user.getId())) throw new BusinessExcetion(ErrorCode.POWERLESS,"创建者不能退出自己的队伍");
            if(baseMapper.updateById(team) <= 0) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR);
            return userTeamMapper.delete(userTeamQueryWrapper) > 0;
        } else throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户不在该队伍中");
    }


    /**
     * 加入队伍
     * @param team
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean joinTeam(JoinTeamRequest team, HttpServletRequest request) {
        if(team.getId()==null || team.getId() <= 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "队伍ID错误");
        RLock lock = redissonClient.getLock("gasen:findmeetbackend:team:join");
        try {
            while(true) {
                if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("加入队伍："+Thread.currentThread().getName()+"getLock");
                    User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("user_id", user.getId());
                    if(userTeamMapper.selectCount(userTeamQueryWrapper)>=5) {
                        throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户已加入5个队伍");
                    }
                    userTeamQueryWrapper.eq("team_id", team.getId()).eq("user_id", user.getId());
                    if(userTeamMapper.selectCount(userTeamQueryWrapper) > 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户已加入该队伍");
                    Team tarteam = lambdaQuery().eq(Team::getId, team.getId()).one();
                    if(TeamEnum.getByState(tarteam.getState())==TeamEnum.PRIVATE) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "该队伍为私有");
                    else if(TeamEnum.getByState(tarteam.getState())==TeamEnum.ENCRYPTION && team.getPassword()!=null && (!tarteam.getPassword().equals(team.getPassword()))) {
                            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "密码错误");
                    }
                    UserTeam userTeam = new UserTeam();
                    userTeam.setTeamId(tarteam.getId()).setUserId(user.getId());
                    if(userTeamMapper.insert(userTeam) <= 0) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR);
                    tarteam.setMaxNum(tarteam.getMaxNum()+1);
                    if(baseMapper.updateById(tarteam) <= 0) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR);
                    return true;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(lock.isHeldByCurrentThread()) {
                System.out.println("加入队伍："+Thread.currentThread().getName()+"freeLock");
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 查询用户加入的队伍
     * @param request
     * @return
     */
    @Override
    public List<Team> queryUserJoinTeam(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        List<Integer> list = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("user_id", user.getId())).stream().map(UserTeam::getTeamId).toList();
        if(!list.isEmpty())
            return baseMapper.selectBatchIds(list);
        else
            return new ArrayList<>();
    }

    /**
     * 查询用户加入的队伍成员
     * @param teamId
     * @param request
     * @return
     */
    @Override
    public List<User> queryUserJoinTeamMembers(int teamId, HttpServletRequest request) {
        if(baseMapper.selectById(teamId) == null) {
            throw new BusinessExcetion(ErrorCode.TEAM_NOT_EXIST);
        }
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        List<Integer> memList = userTeamMapper.selectList(userTeamQueryWrapper).stream().map(UserTeam::getUserId).toList();
        return userService.getBaseMapper().selectBatchIds(memList).stream().map(UserController::getSaftyUser).toList();
    }


    /**
     * 判断队伍信息是否合法
     * */
    public void inspectTeam(TeamRequest team, boolean isAdd) {
        log.info("请求体信息"+team.toString());
        if(StringUtils.isBlank(team.getName())) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR,"队伍名称为空");
        if(team.getCreateUser() < 0 ) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "创建者id错误");
        if(AlgorithmUtils.isStringOk(team.getName())) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "队伍名称不合法");
        if(TeamEnum.getByState(team.getState())==null) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "队伍状态不存在");
        if(TeamEnum.getByState(team.getState())==TeamEnum.PUBLIC && !team.getPassword().isEmpty()) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "公开队伍密码应该为空");
        if(TeamEnum.getByState(team.getState())==TeamEnum.ENCRYPTION && team.getPassword().isEmpty()) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "加密队伍密码不能为空");
        if(isAdd){
            if(lambdaQuery().eq(Team::getName,team.getName()).count() > 0) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "队伍名称已存在");
            if(lambdaQuery().eq(Team::getCreateUser, team.getCreateUser()).count()>=5) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "创建者创建队伍数量超过5");
        }
    }
}

package com.gasen.findmeetbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gasen.findmeetbackend.common.ErrorCode;
import com.gasen.findmeetbackend.controller.UserController;
import com.gasen.findmeetbackend.exception.BusinessExcetion;
import com.gasen.findmeetbackend.model.Request.UserBannedDaysRequest;
import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.mapper.UserMapper;
import com.gasen.findmeetbackend.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.findmeetbackend.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gasen.findmeetbackend.constant.UserConstant.*;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public long userRegister(String userAccount, String password) {
        inspectUser(userAccount, password);
        //检查用户名是否重复
        if (lambdaQuery().eq(User::getUserAccount, userAccount).exists()) {
            throw new BusinessExcetion(ErrorCode.USER_EXIST,"用户名重复");
        }
        //密码加密
        password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());
        boolean save = this.save(new User(userAccount, password));
        return save ? 1 : 0;
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        inspectUser(userAccount, password);
        //密码加密
        password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());
        User user = lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getPassword, password).one();
        if (user == null) {
            throw new BusinessExcetion(ErrorCode.USER_NOT_EXIST,"用户不存在或密码不正确");
        }
        //检查是否被封禁
        if(isUserBanned(user))
            return new User().setUnblockingTime(user.getUnblockingTime());
        //登录
        request.getSession().setAttribute(USER_LOGIN_IN, UserController.getSaftyUser(user));
        userSignInUpExp(user);
        return user;
    }
    /**
     * 退出登录
     * */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if(request.getSession().getAttribute(USER_LOGIN_IN)==null) {
            throw new BusinessExcetion(ErrorCode.USER_NOT_LOGIN,"用户未登录");
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        log.info(user.getUserAccount()+"登出");
        request.getSession().removeAttribute(USER_LOGIN_IN);
        return true;
    }

    /**
     * 检查用户是否被封禁
     * */
    public boolean isUserBanned(User user) {
        if(user.getState()==banned)  {
            if(LocalDateTime.now().isAfter(user.getUnblockingTime())) {
                user.setState(USER);
                updateById(user);
                log.info("用户{}已解封", user.getUserAccount());
                return false;
            } else return true;
        } else return false;
    }

    /**
     * 用户封禁
     */
    @Override
    public Boolean userBannedDays(UserBannedDaysRequest userBannedDaysRequest) {
        if(lambdaQuery().eq(User::getId, userBannedDaysRequest.getId()).exists()) {
            lambdaUpdate().eq(User::getId, userBannedDaysRequest.getId()).set(User::getUnblockingTime, LocalDateTime.now().plusDays(userBannedDaysRequest.getDays())).update(new User());
            lambdaUpdate().eq(User::getId, userBannedDaysRequest.getId()).set(User::getState, banned).update(new User());
            return true;
        }
        throw new BusinessExcetion(ErrorCode.USER_NOT_EXIST, "用户不存在");
    }


    /**
     * 获取所有用户
     * */
    @Override
    public List<User> usersList() {
        return baseMapper.selectList(null);
    }

    /**
     * 根据标签搜索用户
     * @param tags
     * @return List<User>
     */
    @Override
    public List<User> selectByTags(List<String> tags) {
        /*sql拼接
        if(CollectionUtils.isEmpty(tags)) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for(String tag:tags) {
            userQueryWrapper.or().like("tags", tag);
        }
        List<User> users = baseMapper.selectList(userQueryWrapper);
        return users.stream().map(UserController::getSaftyUser).toList();
        */
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> users = baseMapper.selectList(userQueryWrapper);
        return users.stream().filter(user -> {
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags)) return false;
            Gson gson = new Gson();
            Set<String> tagsSet = gson.fromJson(userTags, new TypeToken<Set<String>>() {
            }.getType());
            for (String tag : tagsSet) {
                if (tags.contains(tag))
                    return true;
            }
            return false;
        }).map(UserController::getSaftyUser).toList();
    }

    /**
     * 匹配伙伴
     * @param user
     * @return
     */
    @Override
    public List<User> match(User user) {
        //取到用户标签
        Gson gson = new Gson();
        List<String> userTags = gson.fromJson(baseMapper.selectById(user.getId()).getTags(), new TypeToken<List<String>>() {
        }.getType());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //排除自己
        userQueryWrapper.ne("id", user.getId());
        userQueryWrapper.isNotNull("tags");
        userQueryWrapper.select("id","tags");
        List<User> users = baseMapper.selectList(userQueryWrapper);
        List<Pair<Integer, User>> matchUsersList = new ArrayList<>();
        for(User u : users) {
            List<String> uTags = gson.fromJson(u.getTags(), new TypeToken<List<String>>() {
            }.getType());
            int i = AlgorithmUtils.minDistance(userTags, uTags);
            matchUsersList.add(new Pair<>(i,u));
        }
        List<Pair<Integer, User>> topUsersList = matchUsersList.stream().sorted(Comparator.comparingInt(Pair::getKey)).limit(5).toList();
        List<Integer> matchedUsersList = topUsersList.stream().map(pair -> pair.getValue().getId()).toList();
        QueryWrapper<User> userQueryWrapper1 = new QueryWrapper<>();
        userQueryWrapper1.in("id", matchedUsersList);
        List<User> matchestUsers = baseMapper.selectList(userQueryWrapper1).stream().map(UserController::getSaftyUser).toList();
        List<User> finalMatchestUsers = new ArrayList<>();
        for(int i : matchedUsersList) {
            for(User j : matchestUsers) {
                if(i == j.getId()) {
                    finalMatchestUsers.add(j);
                    break;
                }
            }
        }
        return finalMatchestUsers;
    }


    /**
     * 判断用户是否合法
     * */
    public void inspectUser(String userAccount, String password) {
        if(StringUtils.isAnyBlank(userAccount, password)) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR,"用户名或密码为空");
        if(userAccount.length() < 6 || password.length() < 6) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户名或密码少于6位");
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户名不能包含特殊字符");
        }
    }

    /**
     * 登录增加经验
     * 登录增加（当前经验+随机1-99）
     * 连续登录增加（当前经验+随机1-99）*（1-3）
     * */
    public void userSignInUpExp(User user) {
        LocalDateTime userSignIn = user.getSignIn();
        if(userSignIn != null) {
            LocalDate signIn = userSignIn.toLocalDate();
            int exp = user.getExp();
            int grade = user.getGrade();
            if(LocalDate.now().isAfter(signIn) && Period.between(signIn, LocalDate.now()).getDays() == 1) {
                Random random = new Random();
                exp = exp + random.nextInt(1, 100)*random.nextInt(1,4);
                while(grade*10<=exp) {
                    grade++;
                    exp -= grade*10;
                }
                user.setExp(exp).setGrade(grade);
            }
        }
        user.setSignIn(LocalDateTime.now());
        //更新数据库
        boolean b = this.updateById(user);
        if(!b) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR,"增加经验失败");
    }

}

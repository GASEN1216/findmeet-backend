package com.gasen.findmeetbackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.findmeetbackend.common.BaseResponse;
import com.gasen.findmeetbackend.common.ErrorCode;
import com.gasen.findmeetbackend.common.PageRequest;
import com.gasen.findmeetbackend.common.ResultUtils;
import com.gasen.findmeetbackend.exception.BusinessExcetion;
import com.gasen.findmeetbackend.mapper.UserMapper;
import com.gasen.findmeetbackend.model.Request.UserBannedDaysRequest;
import com.gasen.findmeetbackend.model.Request.UserRegisterLoginRequest;
import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gasen.findmeetbackend.constant.UserConstant.*;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    //TODO：ZSet做排行榜

    /**
     * 更新用户
     * User传进来要有id
     * */
    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public BaseResponse updateUser(@RequestBody User user, HttpServletRequest request) {
        //1.是否为管理员或自己
        User current_user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        if(current_user.getState() == ADMIN || user.getId().equals(current_user.getId())) {
            //2.判断用户是否存在
            if(userService.lambdaQuery().eq(User::getId, user.getId()).exists()) {
                //TODO：判断是否有更新信息，未更新直接返回
                //3.更新用户信息
                log.info("id为"+user.getId()+"的用户更新信息");
                if(userService.updateById(user))
                    //4.返回更新后的用户信息
                    return ResultUtils.success(getSaftyUser(userMapper.selectById(user.getId())));
                else return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"更新用户信息失败");
            } else return ResultUtils.error(ErrorCode.USER_NOT_EXIST);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }

    /**
     * 删除用户
     * */
    @PostMapping("/delete")
    public BaseResponse deleteUser(@RequestParam("id") int id, HttpServletRequest request) {
        //1.是否为管理员
        if(isAdmin(request)) {
            //2.判断用户是否存在
            if(userService.lambdaQuery().eq(User::getId, id).exists()) {
                //3.删除用户
                userMapper.deleteById(id);
                log.info("id为"+id+"的用户删除成功");
                return ResultUtils.success("id为"+id+"的用户删除成功");
            } else return ResultUtils.error(ErrorCode.USER_NOT_EXIST);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    public BaseResponse getCurrentUser(HttpServletRequest request) {
        //1.获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        //2.不为null返回安全用户信息
        if(user!=null) {
            log.info("请求获取当前用户信息："+user.getUserAccount());
            //获取最新的用户信息
            User latestUser = getSaftyUser(userMapper.selectById(user.getId()));
            return ResultUtils.success(latestUser);
        }

        //3.为null返回用户未登录
        else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN);
    }

    /**
     * 获取推荐用户信息long pageNum, long pageSize
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> getRecommendUser(PageRequest pageRequest, HttpServletRequest request) {
        long pageNum = pageRequest.getPageNum();
        long pageSize = pageRequest.getPageSize();
        if(isAdmin(request)) {
            //有缓存则直接返回缓存
            User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
            String redisKey = String.format("findmeet:user:recommend:%s:%s:%s", user.getId(),pageNum,pageSize);
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
            if(userPage!=null) {
                return ResultUtils.success(userPage);
            }
            //无缓存则查询数据库
            // 使用 MyBatis Plus 的分页查询功能,如果想暂时查询所有将pageSize<0
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            // 去除自己
            queryWrapper.ne("id", user.getId());
            Page<User> userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
            valueOperations.set(redisKey,userList,10, TimeUnit.SECONDS);
            log.info("接口 /recommend 缓存成功");
            return ResultUtils.success(userList);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }


    /**
     * 根据标签搜索用户
     * @param tags
     * @return BaseResponse
     */
    @Operation(summary = "根据标签搜索用户")
    @GetMapping("/search/tags")
    public BaseResponse searchByTags(@RequestParam List<String> tags) {
        if(CollectionUtils.isEmpty(tags))
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR);
        return ResultUtils.success(userService.selectByTags(tags));
    }

    /**
     * 用户注册
     * */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterLoginRequest user) {
        if(user==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"用户为空");
        String userAccount = user.getUserAccount();
        String password = user.getPassword();
        userDetail(user,"注册");
        if(StringUtils.isAnyBlank(userAccount,password)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"账户名或密码为空");
        }
        long l = userService.userRegister(userAccount, password);
        return ResultUtils.success(l);
    }


    /**
     * 用户登录暨签到
     * */
    @PostMapping("/login")
    public BaseResponse<?> login(@RequestBody UserRegisterLoginRequest user, HttpServletRequest request) {
        if(user==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"用户为空");
        String userAccount = user.getUserAccount();
        String password = user.getPassword();
        userDetail(user,"登录");
        if(StringUtils.isAnyBlank(userAccount,password)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"账户名或密码为空");
        }
        User user1 = userService.userLogin(userAccount, password, request);
        if(user1.getUnblockingTime()!=null&&user1.getState()==null)
            return ResultUtils.error(ErrorCode.BANNED_USER,user1.getUnblockingTime());
        User saftyUser = getSaftyUser(user1);
        return ResultUtils.success(saftyUser);
    }

    /**
     * 用户登出
     * */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        boolean logout = userService.userLogout(request);
        return ResultUtils.success(logout);
    }

    /**
     * 用户封禁
     * */
    @PostMapping("/banned")
    public BaseResponse banned(@RequestBody UserBannedDaysRequest userBannedDaysRequest, HttpServletRequest request) {
        if(!isAdmin(request)) return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
        if(userBannedDaysRequest ==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"参数为空");
        log.info("ID为"+userBannedDaysRequest.getId()+"的用户被封禁"+userBannedDaysRequest.getDays()+"天");
        Boolean banned = userService.userBannedDays(userBannedDaysRequest);
        return ResultUtils.success(banned);
    }

    /**
     * 匹配相似标签的伙伴
     * @param request
     * @return BaseResponse
     */
    @GetMapping("/match")
    public BaseResponse match(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        if(user==null)
            return ResultUtils.error(ErrorCode.USER_NOT_LOGIN);
        return ResultUtils.success(userService.match(user));
    }

    /**
     * 用户信息
     * */
    public void userDetail(UserRegisterLoginRequest user, String behavior) {
        log.info("用户"+user.getUserAccount()+behavior+"："+ user);
    }


    /**
     * 获取安全用户信息
     * */
    public static User getSaftyUser(User user) {
        User saftyUser = new User();
        saftyUser.setId(user.getId());
        saftyUser.setUserAccount(user.getUserAccount());
        saftyUser.setUserName(user.getUserName());
        saftyUser.setAvatarUrl(user.getAvatarUrl());
        saftyUser.setGender(user.getGender());
        saftyUser.setGrade(user.getGrade());
        saftyUser.setExp(user.getExp());
        saftyUser.setState(user.getState());
        saftyUser.setEmail(user.getEmail());
        saftyUser.setPhone(user.getPhone());
        saftyUser.setSignIn(user.getSignIn());
        saftyUser.setUnblockingTime(user.getUnblockingTime());
        saftyUser.setTags(user.getTags());
        return saftyUser;
    }

    /**
     * 是否是管理员
     * */
    public static boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        return user != null && user.getState() == ADMIN;
    }
}

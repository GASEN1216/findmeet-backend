package com.gasen.findmeetbackend.service.impl;

import com.gasen.findmeetbackend.model.domain.UserTeam;
import com.gasen.findmeetbackend.mapper.UserTeamMapper;
import com.gasen.findmeetbackend.service.IUserTeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户队伍表 服务实现类
 * </p>
 *
 * @author gasen
 * @since 2024-03-13
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements IUserTeamService {

}

package com.gasen.findmeetbackend.model.Request;

import lombok.Data;

/**
 * 加入队伍请求类
 * @author GASEN
 * @date 2024/3/14 13:41
 * @classType description
 */
@Data
public class JoinTeamRequest {
    private Integer id;
    private String password;
}

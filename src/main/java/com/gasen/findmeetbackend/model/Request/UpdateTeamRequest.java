package com.gasen.findmeetbackend.model.Request;

import lombok.Data;

/**
 * 更新队伍信息请求类
 * @author GASEN
 * @date 2024/3/14 14:07
 * @classType description
 */
@Data
public class UpdateTeamRequest {
    private Integer id;
    private Integer createUser;
    private String name;
    private String description;
    private Integer state;
    private String password;
}

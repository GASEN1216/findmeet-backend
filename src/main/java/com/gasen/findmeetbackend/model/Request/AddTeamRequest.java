package com.gasen.findmeetbackend.model.Request;

import lombok.Data;

/**
 * 创建队伍请求类
 * @author GASEN
 * @date 2024/3/14 13:46
 * @classType description
 */
@Data
public class AddTeamRequest {
    private String name;
    private String description;
    private Integer state;
    private String password;
}

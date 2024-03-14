package com.gasen.findmeetbackend.model.Request;

import lombok.Data;

/**
 * 队伍请求总类
 * @author GASEN
 * @date 2024/3/13 17:36
 * @classType description
 */
@Data
public class TeamRequest {
    private Integer id;
    private Integer createUser;
    private String name;
    private String description;
    private Integer maxNum;
    private Integer state;
    private String password;
}

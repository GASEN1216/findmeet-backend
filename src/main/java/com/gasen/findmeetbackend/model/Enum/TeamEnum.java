package com.gasen.findmeetbackend.model.Enum;

/**
 * 队伍状态枚举类
 * @author GASEN
 * @date 2024/3/13 18:11
 * @classType description
 */
public enum TeamEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    ENCRYPTION(2, "加密");

    private int state;
    private String description;

    TeamEnum(int state, String description) {
        this.state = state;
        this.description = description;
    }

    // 添加查找方法
    public static TeamEnum getByState(int state) {
        for (TeamEnum team : TeamEnum.values()) {
            if (team.getState() == state) {
                return team;
            }
        }
        return null; // 如果没有找到匹配项，则返回null
    }

    public int getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }
}

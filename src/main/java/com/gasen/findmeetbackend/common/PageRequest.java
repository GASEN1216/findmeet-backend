package com.gasen.findmeetbackend.common;

import lombok.Data;

/**
 * 分页请求类
 * @author GASEN
 * @date 2024/3/13 10:20
 * @classType description
 */
@Data
public class PageRequest {
    private long pageNum;
    private long pageSize;
}

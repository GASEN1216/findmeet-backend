package com.gasen.findmeetbackend.once.importuser;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel数据去重
 * @author GASEN
 * @date 2024/3/10 9:50
 * @classType description
 */
public class excludeRepeat {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Administrator\\Desktop\\MOCK_DATA.xlsx";
        List<importUser> list = EasyExcel.read(filePath).head(importUser.class).sheet().doReadSync();;
        System.out.println("总数："+list.size());
        Map<String, List<importUser>> map = list.stream().filter(importUser -> StringUtils.isNotEmpty(importUser.getUserAccount()))
                .collect(Collectors.groupingBy(importUser::getUserAccount));
        System.out.println("去重后："+map.size());
    }

}

package com.gasen.findmeetbackend.once.importuser;

import com.alibaba.excel.EasyExcel;
import com.gasen.findmeetbackend.mapper.UserMapper;
import com.gasen.findmeetbackend.model.domain.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 插入用户
 * @author GASEN
 * @date 2024/3/10 10:15
 * @classType description
 */
@Component
public class InsertUsers {

    @Resource
    private importUserListener importUserListener;

    @Resource
    private UserMapper userMapper;

    //@Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void insertusers() {
        String filePath = "C:\\Users\\Administrator\\Desktop\\MOCK_DATA.xlsx";
        EasyExcel.read(filePath, importUser.class, importUserListener).sheet().doRead();
    }

    //@Scheduled(initialDelay = 0, fixedRate = Long.MAX_VALUE)
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for(int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("gasen");
            user.setUserAccount("gasen");
            user.setPassword("12345678");
            user.setAvatarUrl("mypicture.com");
            user.setGender(0);
            user.setEmail("gasen12345@163.com");
            user.setPhone("112354685");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("插入用户耗时：" + stopWatch.getTotalTimeSeconds() + "s");
    }
}

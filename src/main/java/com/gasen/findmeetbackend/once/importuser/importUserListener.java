package com.gasen.findmeetbackend.once.importuser;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;

import com.gasen.findmeetbackend.model.domain.User;
import com.gasen.findmeetbackend.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

import static com.gasen.findmeetbackend.constant.UserConstant.SALT;

/**
 * @author GASEN
 * @date 2024/3/9 19:03
 * @classType description
 */
// 有个很重要的点 importUserListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
@Component
@Slf4j
public class importUserListener implements ReadListener<importUser> {

    private static final int BATCH_COUNT = 100;

    private List<User> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    @Resource
    private IUserService userService;

    @Override
    public void invoke(importUser data, AnalysisContext context) {
        cachedDataList.add(new User(data.getUserAccount(), data.getPassword()));
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        cachedDataList.forEach(user -> user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword()+SALT).getBytes())));
        userService.saveBatch(cachedDataList);
        log.info("存储数据库成功！");
    }
}
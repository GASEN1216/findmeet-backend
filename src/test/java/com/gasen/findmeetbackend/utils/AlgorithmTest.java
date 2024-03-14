package com.gasen.findmeetbackend.utils;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法测试类
 * @author GASEN
 * @date 2024/3/12 10:25
 * @classType description
 */
@SpringBootTest
@Slf4j
public class AlgorithmTest {

    @Test
    public void test() {
        List<String> tags1 = Arrays.asList("java", "c++", "Python");
        List<String> tags2 = Arrays.asList("c++");
        List<String> tags3 = Arrays.asList("");
        log.info(""+AlgorithmUtils.minDistance(tags1, tags2)+" "+AlgorithmUtils.minDistance(tags1, tags3));
    }
}

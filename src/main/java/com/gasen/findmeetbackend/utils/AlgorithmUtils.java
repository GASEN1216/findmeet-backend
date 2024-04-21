package com.gasen.findmeetbackend.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 算法工具类
 * @author GASEN
 * @date 2024/3/12 10:18
 * @classType description
 */

public class AlgorithmUtils {
    /**
     * 最短距离编辑算法（计算两个标签之间的编辑距离）
     * 原理：https://blog.csdn.net/DBC_121/article/details/104198838
     */
    public static int minDistance(List<String> tags1, List<String> tags2){
        int n = tags1.size();
        int m = tags2.size();

        if(n * m == 0)
            return n + m;

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++){
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++){
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++){
            for (int j = 1; j < m + 1; j++){
                int left = d[i - 1][j] + 1;
                int down = d[i][j - 1] + 1;
                int left_down = d[i - 1][j - 1];
                if (!tags1.get(i - 1).equals(tags2.get(j - 1)))
                    left_down += 1;
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }
        return d[n][m];
    }

    /**
     * 判断字符串是否合法
     * @param s
     * @return boolean
     */
    public static boolean isStringOk(String s){
        // 不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(s);
        return matcher.find();
    }

}

package config;

import java.util.HashMap;
import java.util.Map;

public class Rating {
    private final static Map<String, int[]> ratingMap = new HashMap<>();
    static {

        ratingMap.put("袁绍", new int[] { 34, 56, 30, 67 });
        ratingMap.put("司马懿", new int[] { -1, 40, 40, 27 });
        ratingMap.put("孟获", new int[] { 50, 45, 36, 54 });
        ratingMap.put("徐晃", new int[] { -1, 41, 32, 24 });
        ratingMap.put("曹仁", new int[] { 45, 62, 56, 58 });
        ratingMap.put("曹操", new int[] { 33, 31, 22, 53 });
        ratingMap.put("鲁肃", new int[] { -1, 41, 41, 19 });
        ratingMap.put("陆逊", new int[] { 23, 30, 21, 20 });
        ratingMap.put("太史慈", new int[] { -1, 42, 31, 30 });
        ratingMap.put("郭嘉", new int[] { 62, 54, 48, 51 });
        ratingMap.put("吕布", new int[] { -1, 37, 22, 22 });
        ratingMap.put("荀彧", new int[] { 44, 63, 46, 24 });
        ratingMap.put("袁术", new int[] { 19, 17, 43, 35 });
        ratingMap.put("魏延", new int[] { 10, 29, 33, 28 });
        ratingMap.put("夏侯惇", new int[] { -1, 31, 33, 26 });
        ratingMap.put("颜良文丑", new int[] { -1, 33, 32, 30 });
        ratingMap.put("大乔", new int[] { -1, 35, 25, 15 });
        ratingMap.put("典韦", new int[] { -1, 42, 39, 24 });
        ratingMap.put("刘禅", new int[] { 38, 53, 32, 27 });
        ratingMap.put("贾诩", new int[] { 27, 49, 43, 47 });
        ratingMap.put("关羽", new int[] { 11, 23, 31, 51 });
        ratingMap.put("蔡文姬", new int[] { -1, 43, 42, 18 });
        ratingMap.put("华佗", new int[] { 56, 51, 45, 53 });
        ratingMap.put("庞德", new int[] { -1, 46, 41, 30 });
        ratingMap.put("卧龙-诸葛亮", new int[] { 14, 40, 41, 38 });
        ratingMap.put("赵云", new int[] { -1, 30, 36, 47 });
        ratingMap.put("孙权", new int[] { 32, 33, 20, 38 });
        ratingMap.put("张辽", new int[] { -1, 34, 40, 41 });
        ratingMap.put("刘备", new int[] { 29, 43, 26, 21 });
        ratingMap.put("黄忠", new int[] { -1, 34, 33, 10 });
        ratingMap.put("周泰", new int[] { 54, 69, 70, 56 });
        ratingMap.put("张郃", new int[] { -1, 47, 47, 29 });
        ratingMap.put("夏侯渊", new int[] { -1, 33, 39, 18 });
        ratingMap.put("周瑜", new int[] { -1, 37, 31, 25 });
        ratingMap.put("庞统", new int[] { -1, 36, 32, 52 });
        ratingMap.put("邓艾", new int[] { -1, 42, 29, 53 });
        ratingMap.put("于吉", new int[] { -1, 31, 21, 18 });
        ratingMap.put("马超", new int[] { -1, 26, 30, 23 });
        ratingMap.put("黄盖", new int[] { -1, 29, 28, 15 });
        ratingMap.put("孙坚", new int[] { -1, 43, 41, 38 });
        ratingMap.put("孙策", new int[] { 46, 38, 40, 39 });
        ratingMap.put("黄月英", new int[] { -1, 31, 25, 39 });
        ratingMap.put("曹丕", new int[] { 58, 63, 59, 54 });
        ratingMap.put("张昭张纮", new int[] { 48, 47, 40, 54 });
        ratingMap.put("祝融", new int[] { -1, 45, 46, 51 });
        ratingMap.put("姜维", new int[] { -1, 33, 33, 51 });
        ratingMap.put("孙尚香", new int[] { 44, 39, 30, 32 });
        ratingMap.put("张飞", new int[] { -1, 31, 32, 25 });
        ratingMap.put("诸葛亮", new int[] { -1, 40, 27, 34 });
        ratingMap.put("貂蝉", new int[] { -1, 36, 38, 38 });
        ratingMap.put("董卓", new int[] { 44, 46, 38, 45 });
        ratingMap.put("甘宁", new int[] { -1, 10, 37, 20 });
        ratingMap.put("吕蒙", new int[] { -1, 40, 32, 53 });
        ratingMap.put("张角", new int[] { 28, 37, 32, 24 });
        ratingMap.put("小乔", new int[] { -1, 45, 54, 50 });
        ratingMap.put("甄姬", new int[] { -1, 45, 43, 55 });
        ratingMap.put("左慈", new int[] { 39, 44, 39, 51 });
        ratingMap.put("SP赵云", new int[] { 40, 41, 51, 52 });
        ratingMap.put("神曹操", new int[] { 83, 100, 100, 100 });
        ratingMap.put("神关羽", new int[] { 38, 54, 100, 54 });
        ratingMap.put("神吕布", new int[] { 57, 57, 52, 65 });
        ratingMap.put("神吕蒙", new int[] { 31, 47, 44, 44 });
        ratingMap.put("神司马懿", new int[] { 57, 50, 53, 72 });
        ratingMap.put("神赵云", new int[] { 63, 74, 79, 76 });
        ratingMap.put("神周瑜", new int[] { 30, 51, 51, 32 });
        ratingMap.put("神诸葛亮", new int[] { 64, 77, 64, 43 });
        ratingMap.put("许褚", new int[] { -1, 29, 21, 10 });
        // 华雄

    }

    /**
     * 获取评分
     * 
     * @param name
     * @return
     */
    public static int[] getRating(String name) {
        return ratingMap.get(name);
    }

}

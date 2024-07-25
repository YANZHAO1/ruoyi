package com.ruoyi.kuzi.utils;

public class Test {
    public static void main(String[] args) {
        // 获取当前机器的处理器数目
        int ncpus = Runtime.getRuntime().availableProcessors();
        // 根据公式计算推荐的线程数
        double threads = ncpus * 1.5;
        System.out.println("推荐的线程数: " + threads);

        String a = "aaa;bbb;aaa";
        System.out.println(a.indexOf("aaa"));
        System.out.println(new Integer(1)>new Integer(2));
        System.out.println(new Integer(1)>new Integer(0));

        System.out.println(new Integer(1000).compareTo(1001));

    }
}

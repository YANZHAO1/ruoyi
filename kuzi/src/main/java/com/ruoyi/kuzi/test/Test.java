package com.ruoyi.kuzi.test;

import java.util.Date;

public class Test {
    public static void main(String[] args) {
        // 开始时间
        Long begin = new Date().getTime();


        // 结束时间
        Long end = new Date().getTime();


        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        System.out.println(messages);
    }


}

package com.ruoyi.kuzi.utils;

import com.alibaba.excel.EasyExcel;

import java.util.Date;
import java.util.List;

public class EasyExcelUtils {
//文件大小   easyexcel   excel-streaming-reader
//156M         228s 96%		97s 97%
//13.6M     	 16s			10s

    private static final String EXCEL_FILE_PATH = "E:\\社会工程学\\网络库\\全国车主76万2020年\\全国车主76万2020年.xlsx";
    public static void main(String[] args) {
        // 开始时间
        Long begin = new Date().getTime();

        //EasyExcel.read(EXCEL_FILE_PATH).sheet().doRead();
        List list =   EasyExcel.read(EXCEL_FILE_PATH).sheet().doReadSync();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }

        // 结束时间
        Long end = new Date().getTime();


        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        System.out.println(messages);
    }
}

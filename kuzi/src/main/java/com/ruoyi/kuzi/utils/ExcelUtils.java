package com.ruoyi.kuzi.utils;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelUtils {
//文件大小   easyexcel   excel-streaming-reader
//156M         228s 96%		97s 97%
//13.6M     	 16s			10s

    private static final String EXCEL_FILE_PATH = "E:\\社会工程学\\网络库\\全国车主76万2020年\\全国车主76万2020年.xlsx";

    public static void main(String[] args) throws FileNotFoundException {

        // 开始时间
        Long begin = new Date().getTime();

        File file = new File(EXCEL_FILE_PATH);
        FileInputStream inputStream = new FileInputStream(file);

        Workbook workbook = StreamingReader.builder().rowCacheSize(1000) // 缓存到内存中的行数(默认是10)
                .bufferSize(1024) // 读取资源时，缓存到内存的字节大小(默认是1024)
                .open(inputStream);
        // 获取第一个Shhet
        Sheet sheet = workbook.getSheetAt(0);
        //
        boolean fastRowBoolean = true;
        // monitorjbl只能支持遍历，不能通过指定下标获取
        for (Row row : sheet) {

            // 判断是否首行
            if (fastRowBoolean) {
                // 设置为非首行
                fastRowBoolean = false;

                for (Cell cell : row) {

                }
                continue;
            }

            // 列下标初始化
            int n = 0;
            // 遍历列
            for (Cell cell : row) {
                System.out.print(cell.getStringCellValue() + " ");
            }
        }

        // 结束时间
        Long end = new Date().getTime();


        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        System.out.println(messages);

    }


}

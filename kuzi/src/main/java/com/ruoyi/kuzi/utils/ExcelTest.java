package com.ruoyi.kuzi.utils;

import com.monitorjbl.xlsx.StreamingReader;
import com.ruoyi.system.api.factory.RemoteFileFallbackFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExcelTest {
//文件大小   easyexcel   excel-streaming-reader 硬盘是ssd
//156M         228s 96%		97s 97%  解析加入库   单条：1403s 批处理：93s
//13.6M     	 16s			10s  解析加入库   单条：130s  批处理：10s
//大文件导入 mysql需要配置max_allowed_packet=64M
//    max_allowed_packet建议值
//    默认值：4M（适合大多数中小型应用）
//    中等值：16M - 64M（适合处理稍大的数据包）
//    较大值：128M - 256M（适合处理非常大的数据包）

//    本类中未处理异常情况实际生产中需要优化

    private static final String EXCEL_FILE_PATH = "E:\\社会工程学\\网络库\\新建文件夹\\裤子-1\\1\\公安千万四要素\\户籍数据1.03GB公安专用\\73万.xlsx";
    private static final Logger log = LoggerFactory.getLogger(ExcelTest.class);
    public static void main(String[] args) throws IOException {
        // 开始时间
        Long begin = new Date().getTime();

        File file = new File(EXCEL_FILE_PATH);
        FileInputStream inputStream = new FileInputStream(file);

        log.error("文件名称："+file.getName());
        String table_name = PinyinUtil.getPinYin(file.getName());
        log.error("表名："+table_name);

        List<String> sqlList = new ArrayList<>();
        String insertSqlPrefix  ="INSERT INTO "+table_name+"  (";

        Workbook workbook = StreamingReader.builder().rowCacheSize(1000) // 缓存到内存中的行数(默认是10)
                .bufferSize(1024) // 读取资源时，缓存到内存的字节大小(默认是1024)
                .open(inputStream);
        // 获取第一个Shhet
        Sheet sheet = workbook.getSheetAt(0);

        int titleColumnSize = 0;
        //
        boolean fastRowBoolean = true;
        // monitorjbl只能支持遍历，不能通过指定下标获取
        for (Row row : sheet) {

            // 判断是否首行
            if (fastRowBoolean) {
                // 设置为非首行
                fastRowBoolean = false;


                Map<String,Integer> field_names = new HashMap<>();
                String createTableSQL = "CREATE TABLE IF NOT EXISTS "+table_name+" (";
                for (Cell cell : row) {
                    String field_name=PinyinUtil.getPinYin(cell.getStringCellValue());
                    // 正则表达式：匹配非汉字、字母和数字的字符
                    String regex = "[^\\u4E00-\\u9FA5a-zA-Z0-9]";
                    // 去除特殊字符但保留汉字、字母和数字
                    field_name = field_name.replaceAll(regex, "");

                    Integer fields= 0;
                    if(field_names.get(field_name)!=null){
                        fields= field_names.get(field_name);
                        fields++;
                    }
                    field_names.put(field_name,fields);

                    if(fields.compareTo(0)>0){
                        field_name=field_name+fields;
                    }


                    //log.error("字段名称："+field_name);
                    createTableSQL +=field_name+" VARCHAR(255) NOT NULL, ";
                    insertSqlPrefix +=field_name+",";
                    titleColumnSize++;
                }
                createTableSQL =removeLastChar(createTableSQL.trim(), ',')+" );  ";

                log.error("createTableSQL:"+createTableSQL);
                JDBCUtils.createTable(createTableSQL);

                insertSqlPrefix =  removeLastChar(insertSqlPrefix.trim(), ',')+" ) VALUES ";
                log.error("insertSqlPrefix:"+insertSqlPrefix);
                continue;
            }

           // String insertSql = insertSqlPrefix+" VALUES(";
            String insertSql = " (";
            int columnSize = 0;
            // 遍历列
            for (int j = 0; j < titleColumnSize; j++) {
                if(columnSize>=titleColumnSize){
                    continue;
                }
                String val= "";
                if(row.getCell(j)!=null){
                    val = row.getCell(j).getStringCellValue();
                }
                insertSql +="'"+val+"',";
                columnSize++;
            }

            insertSql =  removeLastChar(insertSql.trim(), ',')+" )";
            //log.error("insertSql =:"+insertSql);

            sqlList.add(insertSql);
        }
        workbook.close();



        //入库
        // 线程数
        final int THREAD_COUNT = 10;
        // 每个线程处理的数据量
        final int BATCH_SIZE = sqlList.size() / THREAD_COUNT;
        log.error("总条数：{},每个线程处理的数据量BATCH_SIZE:{}",sqlList.size(),BATCH_SIZE);
        final String insertSqlPrefixF = insertSqlPrefix;
        // ExecutorService是Java中对线程池定义的一个接口
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        //
        for (int i = 0; i < THREAD_COUNT; i++) {
            // List数据开始下标
            final int startIndex = i * BATCH_SIZE;
            // List数据结束下标
            final int endIndex = (i + 1) * BATCH_SIZE;
            // 线程池执行
            executor.submit(new Runnable() {
                public void run() {

//                    for (int j = startIndex; j < endIndex; j++) {
//                        JDBCUtils.executeUpdate(sqlList.get(j));
//                    }
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    String sql = "";
                    try {
                        conn = JDBCUtils.getConnection();
                        conn.setAutoCommit(false);
                        pstmt = conn.prepareStatement(insertSqlPrefixF);

                        StringBuffer suffix = new StringBuffer();
                        for (int j = startIndex; j < endIndex; j++) {
                            suffix.append(sqlList.get(j)+",");
                        }
                        sql = insertSqlPrefixF + suffix.substring(0, suffix.length() - 1);
                        //log.error("sql:{}",sql);
                        pstmt.addBatch(sql);

                        pstmt.executeBatch();
                        conn.commit();
                        // 初始化拼接sql
                        suffix.setLength(0);
                        //log.error("sql:{}",sql);
                        log.error("startIndex:{},endIndex:{}",startIndex,endIndex);
                    } catch (SQLException e) {
                        log.error("sql:{}",sql);
                        e.printStackTrace();
                    }finally {
                        JDBCUtils.closePreparedStatement(pstmt);
                        JDBCUtils.closeConnection(conn);
                    }
                }
            });
        }
        //关闭线程池,不接受新任务,但会把已添加的任务执行完
        executor.shutdown();
        // 等待所有线程完成任务
        while (!executor.isTerminated()) {}



        // 结束时间
        Long end = new Date().getTime();
        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        log.error(messages);

    }

    public static String removeLastChar(String str, char ch) {
        if(str == null || str.length() == 0) {
            return str; // 如果字符串为空或长度为0，则直接返回原字符串
        }
        // 判断字符串的最后一个字符是否为指定字符
        if(str.charAt(str.length()-1) == ch) {
            // 使用substring方法从0到倒数第二个字符，去掉最后一个指定字符
            return str.substring(0, str.length()-1);
        }
        return str;
    }

}

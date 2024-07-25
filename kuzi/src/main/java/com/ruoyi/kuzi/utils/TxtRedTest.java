package com.ruoyi.kuzi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TxtRedTest {

    //txt分段读取
    //BufferedReader 适用于逐行处理文本内容的场景，例如日志文件处理。
    //RandomAccessFile 适用于需要精确控制读取位置和范围的场景，例如大型文件的特定区域读取。

    //分段解析 用时1413s
    //11g的文本5亿条数据 解析加入库 52831s=14小时
    //入库后  查询 select count(user_phone) from  wei_bo_wu_yi_2019txt
    // 没索引  2330s
    // 有索引 254s



    private static final Logger log = LoggerFactory.getLogger(TxtRedTest.class);

    private static final String EXCEL_FILE_PATH = "E:\\社会工程学\\网络库\\微博五亿2019.txt";
    private static final int LINES_PER_BATCH = 100000; // 每个批次的行数
    private static final int THREAD_COUNT = 15; // 线程数
    //CPU 密集型计算 最佳线程数 = CPU 核数 + 1
    // I/O 密集型的计算 最佳线程数 = CPU 核数 * [ 1 +（I/O 耗时 / CPU 耗时）]
    private static final long BUFFER_SIZE = 100*1024 * 1024; // 100MB 缓冲区大小 每次读取的字节数

    private static final AtomicLong totalBytesProcessed = new AtomicLong(0); // 处理的总字节数
    private static final AtomicLong lastReportedBytes = new AtomicLong(0); // 上次报告的字节数

    public static void main(String[] args) {
        // 开始时间
        Long begin = new Date().getTime();


        ExecutorService executorService  = Executors.newFixedThreadPool(THREAD_COUNT);

        try (RandomAccessFile file = new RandomAccessFile(EXCEL_FILE_PATH, "r")) {
            File afile = new File(EXCEL_FILE_PATH);

            log.error("文件名称："+afile.getName());
            String table_name = PinyinUtil.getPinYin(afile.getName());
            log.error("表名："+table_name);
            String createTableSQL = "CREATE TABLE IF NOT EXISTS "+table_name+" (user_phone VARCHAR(255) NOT NULL,uid VARCHAR(255) NOT NULL);";
            JDBCUtils.createTable(createTableSQL);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String insertSqlPrefix ="INSERT INTO "+table_name+"  (user_phone,uid) VALUES ";

            long fileLength = file.length();
            long partSize = fileLength / THREAD_COUNT;

            for (int i = 0; i < THREAD_COUNT; i++) {
                long start = i * partSize;
                long end = (i == THREAD_COUNT - 1) ? fileLength : start + partSize;
                executorService.submit(() -> processPart(EXCEL_FILE_PATH, start, end,insertSqlPrefix));
            }

            // 启动一个线程定期报告进度
//            new Thread(() -> {
//                while (!executorService.isTerminated()) {
//                    long processed = totalBytesProcessed.get();
//                    if (processed != lastReportedBytes.get()) {
//                        log.error(" 处理进度 Progress: %.2f%%%n", (processed / (double) fileLength) * 100);
//                        lastReportedBytes.set(processed);
//                    }
//                    try {
//                        Thread.sleep(5000); // 每5秒报告一次进度
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }


        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // 结束时间
        Long end = new Date().getTime();
        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        log.error(messages);
    }

    private static void processPart(String filePath, long start, long end,String insertSqlPrefix) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(start);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getFD())))) {
                if (start > 0) {
                    // Skip the partial line at the start if not the first part
                    reader.readLine();
                }

                List<Object[]> batchLines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null && file.getFilePointer() <= end) {
                    //log.error(line);
                    // 这里处理每行数据，例如存储到数据库或执行其他操作
                    batchLines.add(line.trim().split("\t"));
                    if (batchLines.size() >= LINES_PER_BATCH) {
                        insertBatch(batchLines,insertSqlPrefix);
                        batchLines.clear();
                    }
                }
                if (!batchLines.isEmpty()) {
                    insertBatch(batchLines,insertSqlPrefix);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void insertBatch(List<Object[]> batchLines,String insertSqlPrefix) {

        String insertSQL = insertSqlPrefix+" (?, ?)";

        JDBCUtils.executeBatchUpdateNew(insertSQL,batchLines);

    }

    public static void main11(String[] args) {
        // 开始时间
        Long begin = new Date().getTime();


        int linesPerBatch = 10000; // 每次读取的行数

        try (BufferedReader reader = new BufferedReader(new FileReader(EXCEL_FILE_PATH))) {
            String line;
            int lineCount = 0;
            int batchCount = 0;

            File afile = new File(EXCEL_FILE_PATH);
            FileInputStream inputStream = new FileInputStream(afile);

            log.error("文件名称："+afile.getName());
            String table_name = PinyinUtil.getPinYin(afile.getName());
            log.error("表名："+table_name);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS "+table_name+" (user_phone VARCHAR(255) NOT NULL,uid VARCHAR(255) NOT NULL);";
            JDBCUtils.createTable(createTableSQL);

            Connection conn = JDBCUtils.getConnection();
            conn.setAutoCommit(false);
            String insertSqlPrefix ="INSERT INTO "+table_name+"  (user_phone,uid) VALUES ";
            PreparedStatement pstmt = conn.prepareStatement(insertSqlPrefix);

            List<String> batchLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                batchLines.add(line);
                lineCount++;

                if (lineCount >= linesPerBatch) {
                    batchCount++;
                    log.error("Processing Batch " + batchCount);

                    StringBuffer suffix = new StringBuffer();
                    // 逐行处理当前批次的数据
                    for (String lineitem : batchLines) {
                        //log.error(lineitem);
                        String [] arr = lineitem.trim().split("\t");
                        String phone = arr[0];
                        String uid = arr[1];
                        log.error("手机号：{}，UID:{}",phone,uid);
                        // 在这里处理每行数据，例如存储到数据库或执行其他操作
                        suffix.append("('"+phone+"','"+uid+"'),");
                    }

                    pstmt.addBatch(insertSqlPrefix + suffix.substring(0, suffix.length() - 1));
                    pstmt.executeBatch();
                    conn.commit();

                    // 重置计数器和批次列表
                    lineCount = 0;
                    batchLines.clear();
                }
            }

            // 处理最后一批数据
            if (!batchLines.isEmpty()) {
                batchCount++;
                log.error("Processing Batch " + batchCount);
                // 逐行处理当前批次的数据
                for (String lineitem : batchLines) {
                    log.error(lineitem);
                    // 在这里处理每行数据，例如存储到数据库或执行其他操作
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // 结束时间
        Long end = new Date().getTime();
        // 总用时
        String messages ="数据转换花费时间 : "+(end - begin) / 1000 + " s" ;
        log.error(messages);

    }






    public static void main1(String[] args) {
        String filePath = "path/to/your/largefile.txt";
        long bytesPerBatch = 1024 * 1024; // 每次读取的字节数（例如1MB）

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long fileLength = file.length();
            long startPosition = 0;
            int batchCount = 0;

            while (startPosition < fileLength) {
                file.seek(startPosition);

                byte[] buffer = new byte[(int) Math.min(bytesPerBatch, fileLength - startPosition)];
                file.read(buffer);

                System.out.println(new String(buffer)); // 处理读取的数据
                if(1==1){return;}
                batchCount++;
                startPosition += bytesPerBatch;

                System.out.println("Batch " + batchCount + " processed.");
                // 这里可以执行批处理的操作，例如将当前批次的数据存储到数据库或文件中
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

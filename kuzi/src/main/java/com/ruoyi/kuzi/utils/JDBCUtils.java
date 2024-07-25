package com.ruoyi.kuzi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class JDBCUtils {

    private static final Logger log = LoggerFactory.getLogger(JDBCUtils.class);

    // JDBC URL, username and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/ry-cloud-test";
    private static final String USER = "root";
    private static final String PASSWORD = "yanz123";

    // JDBC variables for opening and managing connection
    private static Connection connection;

    // Static block to initialize the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method to create and return a connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                //log.error("getConnection....");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    // Method to close the ResultSet
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to close the Statement
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closePreparedStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to close the Connection
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to execute a query
    public static ResultSet executeQuery(String query, Object... params) {
        ResultSet rs = null;
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    // Method to execute an update (INSERT, UPDATE, DELETE)
    public static int executeUpdate(String query, Object... params) {
        int result = 0;
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int executeUpdate(String query) {
        int result = 0;
        try {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("query =:"+query);
            e.printStackTrace();
        }
        return result;
    }

    // Method to execute a batch update
    public static int[] executeBatchUpdate(String query, List<Object[]> batchParams) {
        int[] result = new int[0];
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            for (Object[] params : batchParams) {
                //log.error("params.length:"+params.length);
                for (int i = 0; i < params.length; i++) {
                    //log.error("params.params[i]:"+params[i]);
                    pstmt.setObject(i + 1, params[i]);
                }
                pstmt.addBatch();
            }
            result = pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
//            closeStatement(pstmt);
//            closeConnection(conn);
        }
        return result;
    }

    // Method to execute a batch update
    public static int[] executeBatchUpdateNew(String query, List<Object[]> batchParams) {
        int[] result = new int[0];
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(query);
            for (Object[] params : batchParams) {
                //log.error("params.length:"+params.length);
                for (int i = 0; i < params.length; i++) {
                    //log.error("params.params[i]:"+params[i]);
                    pstmt.setObject(i + 1, params[i]);
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
//            closeStatement(pstmt);
//            closeConnection(conn);
        }
        return result;
    }

    // Method to create a table

    /**
     *  String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
     *                 + "id INT(11) NOT NULL AUTO_INCREMENT, "
     *                 + "username VARCHAR(50) NOT NULL, "
     *                 + "password VARCHAR(50) NOT NULL, "
     *                 + "PRIMARY KEY (id))";
     * @param createTableSQL
     */
    public static void createTable(String createTableSQL) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(createTableSQL);
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }
}

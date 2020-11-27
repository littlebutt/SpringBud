package org.springbud.util;

import lombok.extern.slf4j.Slf4j;
import org.springbud.orm.support.TableDefinition;

import java.sql.*;
import java.util.Map;

@Slf4j
public class JdbcUtil {
    public static Connection init(String host, int port, String database, String username, String password) throws ClassNotFoundException {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + String.valueOf(port) + "/" + database, username, password);
        }catch(Exception e){
            log.error("Fail to connect database " +  host + ":" + String.valueOf(port) + "/" + database);
            e.printStackTrace();
        }
        return conn;
    }

    public static int execute(Connection conn, String sql, Object[] params){
        PreparedStatement pstmt =null;
        int result = -1;
        try {
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i ++){
                pstmt.setObject(i+1, params[i]);
            }
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Fail to execute " + sql);
            System.out.println(e.getMessage());
        }
        finally {
            try {
                assert pstmt != null;
                pstmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return result;
    }

    public static int create(Connection conn, TableDefinition definition) {
        int returnValue = -1;
        try {
            Statement stmt = conn.createStatement();
            Map<String, String> typeNameMap = definition.getRecord();
            if (typeNameMap.isEmpty())
                return returnValue;
            StringBuffer sql = new StringBuffer("CREATE TABLE " + definition.getName() + " (");
            typeNameMap.forEach((t, n) -> {
                sql.append(t).append(" ").append(n).append(", ");
            });
            sql.deleteCharAt(sql.length() - 2);
            sql.append(");");
            returnValue = stmt.executeUpdate(sql.toString());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return returnValue;
    }

    public static void release(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }
}

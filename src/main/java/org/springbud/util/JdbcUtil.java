package org.springbud.util;

import lombok.extern.slf4j.Slf4j;
import org.springbud.orm.support.TableDefinition;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class JdbcUtil {
    public static Connection init(String host, int port, String database, String username, String password) throws ClassNotFoundException {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + String.valueOf(port) + "/" + database +"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", username, password);
        }catch(Exception e){
            log.error("Fail to connect database " +  host + ":" + String.valueOf(port) + "/" + database);
            e.printStackTrace();
        }
        return conn;
    }

    public static boolean execute(Connection conn, String sql){
        PreparedStatement pstmt =null;
        boolean result = false;
        try {
            pstmt = conn.prepareStatement(sql);
            result = pstmt.execute(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return result;
    }

    public static <E> Set<E> query(Connection conn, String sql, E entity) {
        int numFields = entity.getClass().getDeclaredFields().length;
        Set<E> returnEntities = new HashSet<>();
        try {

            Statement stmt = conn.createStatement();
            ResultSet set = stmt.executeQuery(sql);

            while (set.next()) {
                Object[] objects = new Object[numFields];
                for (int i = 0;i < numFields;i ++) {
                    objects[i] = set.getString(i + 1);
                }

                E newEntity = (E) entity.getClass().getDeclaredConstructor().newInstance(objects);
                returnEntities.add(newEntity);
            }
        } catch (InstantiationException | InvocationTargetException | SQLException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return returnEntities;
    }

    public static int create(Connection conn, TableDefinition definition) {
        int returnValue = -1;
        try {
            Statement stmt = conn.createStatement();
            Map<String, String> typeNameMap = definition.getRecord();
            if (typeNameMap.isEmpty())
                return returnValue;
            StringBuffer sql = new StringBuffer("CREATE TABLE " + definition.getName() + " (");
            typeNameMap.forEach((n, t) -> {
                sql.append(n).append(" ").append(t).append(", ");
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

package org.springbud.orm;

import lombok.extern.slf4j.Slf4j;
import org.springbud.core.ContainerBean;
import org.springbud.exceptions.DatabaseProcessException;
import org.springbud.orm.annotations.Column;
import org.springbud.orm.annotations.DatabaseConfigurer;
import org.springbud.orm.annotations.Id;
import org.springbud.orm.annotations.Table;
import org.springbud.orm.support.TableDefinition;
import org.springbud.util.JdbcUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;

@Slf4j
public class DatabaseProcessor {

    public static Connection conn;

    private static final List<TableDefinition> tableDefinitionList = new ArrayList<>();
    
    private static boolean tablesCreated = false;

    public DatabaseProcessor() {
        if (conn == null) {
            initConnection();
        }
        if (tableDefinitionList == null){
            initTableDefinitionList();
        }
        if (!tablesCreated) {
            createTables();
        }
    }

    private void createTables() {
        if (tableDefinitionList.isEmpty())
            return;

        for (TableDefinition definition : tableDefinitionList) {
            if (JdbcUtil.create(conn, definition) == -1)
                throw new DatabaseProcessException("Fail to create table " + definition.getName());
        }

    }

    private void initConnection() {
        ContainerBean containerBean = ContainerBean.getInstance();
        Set<Class<?>> configurers = containerBean.getClassesByAnnotation(DatabaseConfigurer.class);
        if (configurers.size() != 1) {
            throw new DatabaseProcessException("No or more than one @DatabaseConfigurer annotated class(es)");
        }
        Class<?> configure = null;
        for (Class<?> clazz : configurers)
            configure = clazz;
        DatabaseConfigurer databaseConfigurerTag = configure.getAnnotation(DatabaseConfigurer.class);
        try {
            conn = JdbcUtil.init(databaseConfigurerTag.host(),
                    databaseConfigurerTag.port(),
                    databaseConfigurerTag.database(),
                    databaseConfigurerTag.username(),
                    databaseConfigurerTag.password());
        } catch (ClassNotFoundException e) {
            throw new DatabaseProcessException("Fail to connect database " +  databaseConfigurerTag.host() + ":" + String.valueOf(databaseConfigurerTag.port()) + "/" + databaseConfigurerTag.database());
        }
    }

    private void initTableDefinitionList() {
        ContainerBean containerBean = ContainerBean.getInstance();
        Set<Class<?>> tableSet = containerBean.getClassesByAnnotation(Table.class);
        if (tableSet.isEmpty()) {
            throw new DatabaseProcessException("Cannot find any table");
        }
        for (Class<?> table : tableSet) {
            Table table1Tag = table.getAnnotation(Table.class);
            String tableName = table1Tag.value();
            Set<Field> fields = new HashSet<>();
            Arrays.stream(table.getDeclaredFields()).forEach((field -> {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class))
                    fields.add(field);
            }));
            if (fields.size() == 0) {
                log.warn("Table " + tableName + " does not contain any record");
                continue;
            }
            Map<String, String> typeNameMap = new HashMap<>();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)){
                    Id idTag = field.getAnnotation(Id.class);
                    String type = idTag.type();
                    String name = idTag.name();
                    typeNameMap.put(type, name);

                } else {
                    Column columnTag = field.getAnnotation(Column.class);
                    String type = columnTag.type();
                    String name = columnTag.name();
                    typeNameMap.put(type, name);
                }
            }
            tableDefinitionList.add(new TableDefinition(tableName, typeNameMap));
        }
    }


    public void release() {
        if (conn != null)
            JdbcUtil.release(conn);
    }
}

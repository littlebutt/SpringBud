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

    private static Connection conn;

    private static final Set<TableDefinition> tableDefinitionList = new HashSet<>();
    
    private static boolean tablesCreated = false;

    public DatabaseProcessor() {
        if (conn == null) {
            initConnection();
        }
        if (tableDefinitionList.isEmpty()){
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
                log.error("Fail to create the table " + definition.getName());
        }
        tablesCreated = true;

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
            Map<String, String> typeNameMap = new TreeMap<>();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)){
                    Id idTag = field.getAnnotation(Id.class);
                    String type = idTag.type();
                    String name = idTag.name();
                    typeNameMap.put(name, type);

                } else {
                    Column columnTag = field.getAnnotation(Column.class);
                    String type = columnTag.type();
                    String name = columnTag.name();
                    typeNameMap.put(name, type);
                }
            }
            tableDefinitionList.add(new TableDefinition(tableName, typeNameMap));
        }
    }

    public <E> DatabaseProcessor insert(E entity) {
        if (entity == null)
            throw new DatabaseProcessException("Attempt to insert empty object");
        if (!entity.getClass().isAnnotationPresent(Table.class))
            throw new DatabaseProcessException("The object is not database entity");
        String tableName = entity.getClass().getAnnotation(Table.class).value();
        Map<String, String> typeNameMap = new HashMap<>();
        for (TableDefinition definition : tableDefinitionList) {
            if (definition.getName().equals(tableName))
                typeNameMap = definition.getRecord();
            break;
        }
        if (typeNameMap.isEmpty())
            throw new DatabaseProcessException("Cannot find the table " + tableName);
        insertRecord(entity, tableName, typeNameMap);
        return this;
    }

    private <E> boolean insertRecord(E entity, String tableName, Map<String, String> typeNameMap) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" VALUES(");
        Field[] fields = Arrays.stream(entity.getClass().getDeclaredFields()).filter((field -> {
            return field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class);
        })).toArray(Field[]::new);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String value = field.get(entity).toString();
                sql.append("'").append(value).append("'").append(", ");
            } catch (IllegalAccessException e) {
                throw new DatabaseProcessException("Cannot cast to String from " + field.getName());
            }
        }
        sql.deleteCharAt(sql.length() - 2);
        sql.append(");");
        return JdbcUtil.execute(conn, sql.toString());
    }

    public <E> DatabaseProcessor deleteById(E entity) {
        String tableName = entity.getClass().getAnnotation(Table.class).value();
        if (tableName.isEmpty()) {
            throw new DatabaseProcessException("The entity " + entity.getClass() + " is not @Table annotated class");
        }
        Field[] fields = Arrays.stream(entity.getClass().getDeclaredFields()).filter((field -> {
            return field.isAnnotationPresent(Id.class);
        })).toArray(Field[]::new);
        if (fields.length != 1) {
            throw new DatabaseProcessException("The entity " + entity.getClass() + " contains no or more than one @Id field(s)");
        }
        Field idField = fields[0];
        String name = idField.getAnnotation(Id.class).name();
        idField.setAccessible(true);
        String value = null;
        try {
            value = (String) idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new DatabaseProcessException(e.getMessage());
        }
        if (!JdbcUtil.execute(conn, "DELETE FROM " + tableName + " WHERE " + name + " = '" + value + "';"))
            throw new DatabaseProcessException("Fail to delete entity " + entity.getClass() + " from table " + tableName);
        return this;
    }

    @Deprecated
    public <E> Set<E> selectByField(E entity, Field field) {
        String tableName = entity.getClass().getAnnotation(Table.class).value();
        if (tableName.isEmpty()) {
            throw new DatabaseProcessException("The entity " + entity.getClass() + " is not @Table annotated class");
        }
        String name = null;
        String value = null;
        if (field.isAnnotationPresent(Column.class)) {
            name = field.getAnnotation(Column.class).name();
        }else {
            name = field.getAnnotation(Id.class).name();
        }
        field.setAccessible(true);
        try {
            value = (String) field.get(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return JdbcUtil.query(conn, "SELECT * FROM " + tableName + " WHERE " + name + " = '" + value + "';", entity);
    }

    public void release() {
        if (conn != null)
            JdbcUtil.release(conn);
    }
}

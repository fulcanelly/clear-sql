package me.fulcanelly.clsql.databse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.*;

import org.apache.commons.lang.ClassUtils;

import lombok.SneakyThrows;
import me.fulcanelly.clsql.async.AsyncActorEngine;
import me.fulcanelly.clsql.async.tasks.AsyncTask;
import me.fulcanelly.clsql.databse.tasks.AsyncSQLTask;
import me.fulcanelly.clsql.stop.Stopable;
import net.md_5.bungee.api.ChatColor;


public class SQLQueryHandler implements Stopable {

    final Connection connection;
    final AsyncActorEngine engine;  
    boolean log;

    public SQLQueryHandler(Connection conn, boolean log) {
        connection = conn;
        this.log = log;
        this.engine = new AsyncActorEngine();
    }

    public SQLQueryHandler(Connection conn) {
        this(conn, true);
    }   

    void logString(String to_log) {
        if (log) {
            System.out.println("[" + Thread.currentThread() + "] "+ ChatColor.BLUE + to_log);
        }
    }


    @SneakyThrows
    PreparedStatement setUpPStatementOf(String query, Object ...args) {
        var pstmt = connection
            .prepareStatement(query);
        
        this.setVars(pstmt, args);
        return pstmt;
    }

    @SneakyThrows
    static String getColumnLabelOf(ResultSetMetaData data, int index) {
        return data.getColumnLabel(index);
    }

    @SneakyThrows
    static Object getColumnByIndex(ResultSet set, int index) {
        return set.getObject(index);
    }

    @SneakyThrows
    public Map<String, Object> parseMapOfResultSet(ResultSet set) {

        ResultSetMetaData data = set.getMetaData();
        
        return IntStream.range(1, data.getColumnCount() + 1)
            .boxed()
            .collect(
                Collectors.toMap(
                    i -> getColumnLabelOf(data, i), 
                    i -> getColumnByIndex(set, i)
                )
        );
    }

    @SneakyThrows
    public List< Map<String, Object> > parseListOf(ResultSet set) {

        var list = new ArrayList< Map<String, Object> >();
        
        while (set.next()) {
            list.add(parseMapOfResultSet(set));
        }

        return list;
    }

    @SneakyThrows
    public int syncExecuteUpdate(String query, Object ...args) {
        logString(query);
        return setUpPStatementOf(query, args)
            .executeUpdate();
    }

    @SneakyThrows
    public ResultSet syncExecuteQuery(String query, Object ...args) {
        logString(query);
        return setUpPStatementOf(query, args)
            .executeQuery();
    }

    public void executeUpdate(String query, Object... args) {
        new AsyncSQLTask<>(query, args, this::syncExecuteUpdate, engine)
            .addToQueue();
    }

    public AsyncTask<ResultSet> executeQuery(String query, Object... args) {
        return new AsyncSQLTask<>(query, args, this::syncExecuteQuery, engine)
            .addToQueue();
    }

    @SneakyThrows
    public Optional<Map<String, Object>> safeParseOne(ResultSet set) {
        if (set.next()) {
            return Optional.of(parseMapOfResultSet(set));
        } else {
            return Optional.empty();
        }
    }

    @SneakyThrows
    void dispatchOneItem(PreparedStatement pstmt, int index, Object item) {
        Stream.of(PreparedStatement.class.getDeclaredMethods())
            .filter(meth -> 
                meth.getName().startsWith("set") && 
               !meth.getName().startsWith("setN")
            )
            .filter(one ->
                ClassUtils.isAssignable(one.getParameterTypes()[1], item.getClass(), true)
            )
            .findFirst().orElseThrow(() -> new RuntimeException("Unknown arg type"))
            .invoke(pstmt, index, item);
    }
    
    void setVars(PreparedStatement pstmt, Object ...list) {
        IntStream.range(0, list.length)
            .forEach(index -> dispatchOneItem(pstmt, index + 1, list[index]));
    }

    @Override
    public void stopIt() {
        engine.stopIt();
    }
 
}

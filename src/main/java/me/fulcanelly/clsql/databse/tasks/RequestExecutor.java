package me.fulcanelly.clsql.databse.tasks;

public interface RequestExecutor<G, T> {
    T process(G q, Object[] data);
}


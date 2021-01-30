package me.fulcanelly.clsql.databse.tasks;

import me.fulcanelly.clsql.async.ActorTemplate;
import me.fulcanelly.clsql.async.tasks.ChainAsyncTask;
import me.fulcanelly.clsql.async.tasks.Task;

public class AsyncSQLTask<T> extends ChainAsyncTask<T> {

    public AsyncSQLTask(String query, Object[] args, RequestExecutor<T> executor, ActorTemplate<Task> worker) {
        super(() -> executor.process(query, args), worker);
    }

}
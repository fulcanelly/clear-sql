package me.fulcanelly.clsql.databse.tasks;

import me.fulcanelly.clsql.async.ActorTemplate;
import me.fulcanelly.clsql.async.tasks.ChainAsyncTask;
import me.fulcanelly.clsql.async.tasks.Task;

public class AsyncSQLTask<G, T> extends ChainAsyncTask<T> {

    public AsyncSQLTask(G q, Object[] args, RequestExecutor<G, T> executor, ActorTemplate<Task> worker) {
        super(() -> executor.process(q, args), worker);
    }

}
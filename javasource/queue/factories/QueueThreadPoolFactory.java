package queue.factories;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import queue.entities.QueueConfiguration;

public class QueueThreadPoolFactory {
	
	public ScheduledExecutorService newScheduledThreadPool(QueueConfiguration configuration, QueueThreadFactory factory) {
		ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(configuration.getCorePoolSize(), factory);
		executor.setRemoveOnCancelPolicy(true);
		return executor;
	}

}

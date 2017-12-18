package queue.repositories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.entities.QueueConfiguration;
import queue.factories.QueueThreadFactory;
import queue.factories.QueueThreadPoolFactory;
import queue.proxies.QueueInfo;

public class QueueRepository {
	
	private static ConcurrentHashMap<String, ScheduledThreadPoolExecutor> queueMap = new ConcurrentHashMap<>();

	public static void newQueue (QueueConfiguration configuration, QueueThreadPoolFactory poolFactory, QueueThreadFactory threadFactory) {
		queueMap.put(configuration.getName(), (ScheduledThreadPoolExecutor) poolFactory.newScheduledThreadPool(configuration, threadFactory));
	}
	
	public static ScheduledExecutorService getQueue(String name) {
		return queueMap.get(name);
	}
	
	public static boolean queueExists(String name) {
		return queueMap.keySet().contains(name);
	}
	
	public static List<IMendixObject> getQueueInfos(IContext context) {
		List<IMendixObject> queueInfos = new ArrayList<>();
		Iterator<Entry<String, ScheduledThreadPoolExecutor>> it = queueMap.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, ScheduledThreadPoolExecutor> entry = it.next();
			QueueInfo queueInfo = new QueueInfo(context);

			ScheduledThreadPoolExecutor executor = entry.getValue();
			
			queueInfo.setName(entry.getKey());
			queueInfo.setIsShutDown(executor.isShutdown());
			queueInfo.setIsTerminated(executor.isTerminated());
			queueInfo.setActiveThreads(executor.getActiveCount());
			queueInfo.setCorePoolSize(executor.getCorePoolSize());
			queueInfo.setPoolSize(executor.getPoolSize());
			queueInfo.setCompletedJobCount(executor.getCompletedTaskCount());
			queueInfo.setTotalJobCount(executor.getTaskCount());
			queueInfo.setJobsInQueue(executor.getQueue().size());
			
			queueInfos.add(queueInfo.getMendixObject());
		}
		
		return queueInfos;
	}
}
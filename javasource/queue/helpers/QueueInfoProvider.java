package queue.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.QueueInfo;

public class QueueInfoProvider {
	
	public List<IMendixObject> getQueueInfo(IContext context, ConcurrentHashMap<String, ScheduledThreadPoolExecutor> queueMap) {
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

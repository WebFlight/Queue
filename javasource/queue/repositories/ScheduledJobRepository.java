package queue.repositories;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

public class ScheduledJobRepository {
	private static ScheduledJobRepository jobRepository;
	private static final Object lock = new Object();
	private volatile ConcurrentHashMap<String, ScheduledFuture<?>> scheduledJobMap = new ConcurrentHashMap<>();
	
	protected ScheduledJobRepository() {
		
	}
	
	public static ScheduledJobRepository getInstance() {
		ScheduledJobRepository instance = jobRepository;
		
		if(instance == null) {
			synchronized(lock) {
				instance = jobRepository;
				if (instance == null) {
					instance = new ScheduledJobRepository();
					jobRepository = instance;
				}
			}
		}
		
		return instance;
	}
	
	public void add(IContext context, IMendixObject job, ScheduledFuture<?> future) {
		scheduledJobMap.put(getId(context, job), future);
	}
	
	public ScheduledFuture<?> get(IContext context, IMendixObject job) throws CoreException {
		return scheduledJobMap.get(getId(context, job));
	}
	
	public void remove(IContext context, IMendixObject job) {
		scheduledJobMap.remove(getId(context, job));
	}
	
	public void remove(IContext context, IMendixObject job, int retry) {
		String id = String.valueOf(job.getId().toLong()) + retry;
		scheduledJobMap.remove(id);
	}
	
	private String getId(IContext context, IMendixObject job) {
		return String.valueOf(job.getId().toLong()) + job.getValue(context, "Retry");
	}

}

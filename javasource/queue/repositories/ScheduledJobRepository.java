package queue.repositories;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.UserException;
import com.mendix.systemwideinterfaces.core.UserException.ExceptionCategory;

public final class ScheduledJobRepository {
	private static ScheduledJobRepository jobRepository;
	private static final Object lock = new Object();
	private volatile ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledJobMap = new ConcurrentHashMap<>();
	
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
	
	public void add(IMendixObject job, ScheduledFuture<?> future) {
		if(!contains(job)) {
			scheduledJobMap.put(job.getId().toLong(), future);
		}
	}
	
	public ScheduledFuture<?> get(IMendixObject job) {
		if(contains(job)) {
			return scheduledJobMap.get(job.getId().toLong());
		}
		throw new UserException(ExceptionCategory.DataValidation, "Job could not be retrieved from repository.");
	}
	
	public void remove(IMendixObject job) {
		if(contains(job)) {
			scheduledJobMap.remove(job.getId().toLong());
		}
	}
	
	public boolean contains(IMendixObject job) {
		return scheduledJobMap.containsKey(job.getId().toLong());
	}

}
